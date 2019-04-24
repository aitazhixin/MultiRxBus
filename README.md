基于RxJava2创建的RxBus，提供了三种事件总线，每种事件总线都是带过滤器的：
1，<T>Observable<T> toObservableWithUI(final Object eventId, Class<T> eventType)：通知UI进行页面或状态更新的事件，事件类型eventId, 事件消息类型eventType
    eventId用于过滤消息
    采用单线程调度器，并且有事件缓存功能，事件观察者(UI)可以接收到观察之前的一次事件，在UI未完成跳转而业务状态发生变化的情况下，在UI准备好之后，能够处理前一次事件
    
2，<T>Observable<T> toObservableWithData(final Object eventId, Class<T> eventType)：传感器数据分发总线，采用自定义线程池调度器

3，<T>Observable<T> toObservableWithNotice(final Object eventId, Class<T> eventType)：消息通知总线，采用Computation调度器，线程数固定，由CPU的核数决定，最多线程数与CPU核数相等，
    可以通过设置系统变量的方式修改线程数