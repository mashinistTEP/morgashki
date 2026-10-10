import re, json, os, glob
ROOT='/home/user/morgashki'
CAT=ROOT+'/app/src/main/java/com/mashinist_tep70bs_145/morgashki/model/LocomotiveCatalog.kt'
src=open(CAT,encoding='utf-8').read()
# блоки по code = "..."
blocks=re.split(r'(?=\n\s*Locomotive\()', src)
catalog={}
for b in blocks:
    m=re.search(r'code\s*=\s*"([^"]+)"', b)
    if not m: continue
    code=m.group(1)
    layers={}
    for dm in re.finditer(r'LayerAsset\(R\.drawable\.([a-z0-9_]+),\s*LayerRect\(([\d.\-]+)f,\s*([\d.\-]+)f,\s*([\d.\-]+)f\)\)', b):
        name=dm.group(1); x,y,w=map(float,dm.group(2,3,4))
        lay=name[len(code)+1:] if name.startswith(code+'_') else name
        layers.setdefault(lay, (x,y,w))
    fon=re.search(r'fonRes\s*=\s*R\.drawable\.([a-z0-9_]+)', b)
    route='onRoute = true' in b
    catalog[code]={'layers':layers,'fon':fon.group(1) if fon else None,'route':route}
files={}
for p in glob.glob(ROOT+'/app/src/main/res/drawable-nodpi/*.png'):
    n=os.path.basename(p)[:-4]
    if n.endswith('_fon') or n.endswith('_fon_old'):
        code=n.rsplit('_fon',1)[0]; lay='fon_old' if n.endswith('_fon_old') else 'fon'
    else:
        code,lay=n.rsplit('_',1) if not n.endswith('_projector') else (n[:-10],'projector')
        if n.endswith('_projector_l'): code,lay=n[:-12],'projector_l'
        elif n.endswith('_projector_r'): code,lay=n[:-12],'projector_r'
        elif n.endswith('_projector'): code,lay=n[:-10],'projector'
        elif n.endswith('_red_bufer_l'): code,lay=n[:-12],'red_bufer_l'
        elif n.endswith('_red_bufer_r'): code,lay=n[:-12],'red_bufer_r'
        elif n.endswith('_bufer_l'): code,lay=n[:-8],'bufer_l'
        elif n.endswith('_bufer_r'): code,lay=n[:-8],'bufer_r'
    files.setdefault(code,set()).add(lay)
kordy=json.load(open('/home/user/calibrator/kordy_razmery.json',encoding='utf-8'))
codes=sorted(set(catalog)|set(files)|set(kordy))
print(f"{'код':<14}{'файлы':<6}{'каталог':<8}{'kordy':<6} проблемы")
issues=[]
for c in codes:
    f=files.get(c,set()); k=set(catalog.get(c,{}).get('layers',{})); ko=set(kordy.get(c,{}))
    prob=[]
    route=bool(catalog.get(c,{}).get('route'))
    rename={'chs4t_rzd':'chs4t_313_rzd','tep70bs':'tep70bs_145','er9t':'er9tm'}
    if catalog.get(c) and route:
        prob.append('на маршруте, фона нет (слоёв: %d)'%len(f))
    else:
        missing_files=[l for l in (k|({'fon'} if catalog.get(c) else set())) - f]
        if catalog.get(c) and missing_files: prob.append('НЕТ ФАЙЛОВ: '+','.join(sorted(missing_files)))
        extra=[l for l in f-k-{'fon','fon_old'}]
        if extra and catalog.get(c): prob.append('файлы без каталога: '+','.join(sorted(extra)))
        if catalog.get(c) and not ko: prob.append('координаты только в каталоге (ок)')
    if ko and not catalog.get(c):
        prob.append('рабочий код калибратора для '+rename.get(c,'?'))
    if catalog.get(c) and ko:
        diff=[l for l in k&ko if any(abs(a-b)>0.01 for a,b in zip(catalog[c]['layers'][l], (kordy[c][l]['xPct'],kordy[c][l]['yPct'],kordy[c][l]['wPct'])))]
        if diff: prob.append('координаты расходятся: '+','.join(sorted(diff)))
        nok=[l for l in ko if kordy[c][l].get('visible')==False and l in k]
        if nok: prob.append('скрыт в kordy: '+','.join(sorted(nok)))
    print(f"{c:<14}{len(f):<6}{len(k) if catalog.get(c) else '-':<8}{len(ko):<6} {'; '.join(prob) if prob else 'ок'}")
    if prob: issues.append(c)
print('\nитого кодов:',len(codes),'| с проблемами:',len(issues))
