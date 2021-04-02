# memo

#### 基于JAVA的备忘录(Memo)App

#### 软件概述
这是一个简洁的备忘录APP，具备四大组件以及LBS。主要体现在有多个Activity进行交互，使用Service来设置闹钟响铃时间，设置的时间到后会响铃并震动，通过继承Broadcast Receiver发送Notification；Content Provider则体现在备忘录信息存储于SQLite数据库中，并通过FileProvider来实现图片的拍摄存储，并可以调用手机相册来设置图片；除此之外还添加了定位功能，用户可以直接打开定位页面获取地址信息，也可以点击百度地图来进行选址。除此之外还对备忘录信息实现了分组管理的功能，备忘录信息通过ListView来进行显示及管理。