# FreeCrop Android上的一个图片自由裁剪方案<br>

解决的问题：<br>
1、多点构成的不规则图形裁剪<br>
2、自由选择区域裁剪<br>
3、自定义图片裁剪<br>

用到的技术：<br>
1、基于ScaleDetector和GestureDetector的图片缩放旋转拖动<br>
2、基于画笔Xfermode的画布裁剪<br>
3、View的事件拦截机制<br>
