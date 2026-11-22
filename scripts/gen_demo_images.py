# -*- coding: utf-8 -*-
"""生成 Lumos 商城演示图片（商品/轮播/资讯）"""
import os
from PIL import Image, ImageDraw, ImageFont

OUT = os.path.join(os.path.dirname(__file__), '..', 'lumos-server', 'src', 'main', 'resources', 'static', 'uploads', 'demo')
os.makedirs(OUT, exist_ok=True)

PALETTES = [
    ((64, 158, 255), (120, 200, 255)),
    ((103, 194, 58), (180, 230, 150)),
    ((230, 162, 60), (250, 210, 140)),
    ((245, 108, 108), (250, 180, 160)),
    ((155, 105, 245), (205, 175, 250)),
    ((40, 180, 170), (140, 225, 215)),
]


def font(size):
    for p in (r'C:\Windows\Fonts\msyh.ttc', r'C:\Windows\Fonts\simhei.ttf'):
        if os.path.exists(p):
            return ImageFont.truetype(p, size)
    return ImageFont.load_default()


def make(name, text, w=800, h=600, i=0):
    c1, c2 = PALETTES[i % len(PALETTES)]
    img = Image.new('RGB', (w, h))
    d = ImageDraw.Draw(img)
    for y in range(h):
        t = y / h
        d.line([(0, y), (w, y)], fill=tuple(int(c1[k] + (c2[k] - c1[k]) * t) for k in range(3)))
    f = font(48 if w >= 800 else 36)
    bbox = d.textbbox((0, 0), text, font=f)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    d.text(((w - tw) / 2, (h - th) / 2), text, font=f, fill=(255, 255, 255))
    img.save(os.path.join(OUT, name), 'JPEG', quality=82)
    print('ok', name)


items = [
    ('phone.jpg', 'Lumos X1 智能手机', 0),
    ('earphone.jpg', 'Lumos Air 蓝牙耳机', 4),
    ('laptop.jpg', '超薄笔记本电脑', 1),
    ('washer.jpg', '变频滚筒洗衣机', 2),
    ('purifier.jpg', '家用空气净化器', 5),
    ('hoodie.jpg', '纯棉连帽卫衣', 3),
    ('shoes.jpg', '轻便运动鞋', 0),
    ('nuts.jpg', '每日坚果 750g', 2),
    ('tea.jpg', '高山乌龙茶', 1),
    ('book.jpg', '算法导论 第3版', 4),
    ('news1.jpg', 'Lumos 商城上线公告', 0),
    ('news2.jpg', '双十一狂欢节', 3),
    ('news3.jpg', '电商行业趋势观察', 5),
]
for name, text, i in items:
    make(name, text, i=i)

make('banner1.jpg', 'Lumos X1 新品上市', w=1600, h=500, i=0)
make('banner2.jpg', '双十一 满300减50', w=1600, h=500, i=3)
make('banner3.jpg', '图书专区 全场特惠', w=1600, h=500, i=4)
