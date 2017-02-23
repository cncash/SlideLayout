## Android仿酷狗音乐自定义侧滑菜单控件简单实现

> 随着Android的不断成熟，许多绚丽的效果也在不断的被大家开发出来，其中侧滑的效果用到的项目很多，用的好的更是给吸引了很多用户。国内像QQ和酷狗App的侧滑就很给力，所以查了一些资料，并结合ViewDragHelper辅助类，做了一种比较简单的侧滑实现方式。


<!-- more -->

### 一、实现效果图
* 实现的效果基本跟酷狗App差不多，因为就是仿造酷狗的~~

![预览图](http://oibrygxgr.bkt.clouddn.com/kugou_preview.gif)

### 二、实现原理

* SlideLayout控件使用的是ViewDragHelper辅助类来实现的。ViewDragHelper是一个实现View的拖拽的神器，它把View的拖拽操作变得特别的简单，不熟悉ViewDragHelper的同学请先上[传送门](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2014/0911/1680.html)。

* 要实现拖拽，首先需要将SlideLayout和ViewDragHelper关联起来，然后将SlideLayout的事件交给ViewDragHelper来处理，然后在ViewDragHelper提供的回调里就可以对View进行各种操作。不过拖拽的原理都是差不多的，通过水平或者竖直的移动ViewGroup,然后不断的layout和invalidate进行重绘显示。

* 在滑动的过程中，除了要不断的计算滑动的位置和重绘界面，还需要对子容器进行不同的动画操作，这里使用的是ViewHelper类对View做平移缩放和渐变等动画。

* 另外还使用枚举来记录SlideLayout侧滑的状态，包括关闭，打开和正在滑动。并且提供PanelSlideListener监听滑动的状态。这样就可以根据不同的状态做不同的操作。比如手动打开侧滑，关闭侧滑等等。


### 三、逻辑分析
> 这个项目实现的逻辑其实并不难，只需要计算出ViewGroup滑动的位置，然后重绘就行，其次还需要计算控件缩放和拉伸的比例等等。当然对各种View的操作方法还是要比较熟悉，不然搞不明白有些逻辑要做这里做。

#### 1. SlideLayout应该作为一个控件容器来包容两个子容器，一个菜单容器，一个主容器，首先我们需要获取SlideLayout容器的宽高和两个子容器对象

* 在View的onSizeChanged()方法里获取SlideLayout的宽高，此时控件已经测量完成

         /**
           * 当控件的宽高发生变化时会回调这个方法，可以用来测量控件的宽高
           *
           * @param w
           * @param h
           * @param oldw
           * @param oldh
           */
          @Override
          protected void onSizeChanged(int w, int h, int oldw, int oldh) {
              super.onSizeChanged(w, h, oldw, oldh);
              mSlideHeight = getMeasuredHeight();
              mSlideWidth = getMeasuredWidth();
              /**
               * 初始化拖动的范围
               * 默认为屏幕宽度的60%
               */
              mSlideRange = (int) (mSlideWidth * mRangePercent);
          }



* 在View的onFinishInflate()方法里可以获取容器对象，此时布局已经填充

          /**
           * 当View填充结束时会调用这个方法，可以获取子View对象
           */
          @Override
          protected void onFinishInflate() {
              super.onFinishInflate();
              if (getChildCount() < 2) {
                  throw new IllegalStateException("SlideLayout控件的子View必须大于2个");
              }
              if (!((getChildAt(0) instanceof ViewGroup) && (getChildAt(1) instanceof ViewGroup))) {
                  throw new IllegalArgumentException("SlideLayout控件的子View必须是ViewGroup");
              }
              mMenuContainer = (ViewGroup) getChildAt(0);
              mMainContainer = (ViewGroup) getChildAt(1);
          }


#### 2. 获取到了需要的属性和对象之后，就可以将SlideLayout和ViewDragHelper进行绑定
* 首先在控件的构造里创建ViewDragHelper对象，创建完之后会有一个回调，而我们对View的各种操作就是在回调的各种方法里进行的

          /** View的滑动的辅助类，在回调里监听View的各种操作
           * @param forParent 要进行触摸滑动的父控件
           * @param sensitivity 控件滑动的速度，敏感度，1.0f正常
           * @param cb  对View的事件发生改变的回调
           */
          mDragHelper = ViewDragHelper.create(this, 1.0f, mViewCallback);

* 创建对象之后，如果此时就对View进行操作是没有效果的，因为还需要把SlideLayout的处理事件传递给ViewDragHelper

          /**
            * 转交拦截事件给辅助类
            *
            * @param ev
            * @return
            */
           @Override
           public boolean onInterceptTouchEvent(MotionEvent ev) {
               final int action = MotionEventCompat.getActionMasked(ev);
               if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                   mDragHelper.cancel();
                   return false;
               }
               return mDragHelper.shouldInterceptTouchEvent(ev);
           }

           /**
            * 转交触摸事件给辅助类
            *
            * @param event
            * @return
            */
           @Override
           public boolean onTouchEvent(MotionEvent event) {
               try {
                   mDragHelper.processTouchEvent(event);
               } catch (Exception e) {
                   e.printStackTrace();
               }
               return true;
           }


* 最重要的地方就是ViewDragHelper的回调了，里面有很多方法，每一个都很重要，这里列举一个对容器的滑动处理方法onViewPositionChanged()。其实逻辑也是比较简单，就是判断当前滑动的是哪一个容器，计算容器的左边界值，然后对容器进行重绘

          /**
           * 当子View的位置发送改变时回调
           * @param changedView 改变的子View
           * @param left 距离左边界距离
           * @param top 距离顶部距离
           * @param dx 水平滑动距离差
           * @param dy 竖直滑动距离差
           */
          @Override
          public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
              /**
               * 将菜单面板的移动量给主面板
               */
              if (changedView == mMenuContainer) {
                  mMenuContainer.layout(0, 0, mSlideWidth, mSlideHeight);
                  int newLeft = mMainContainer.getLeft() + dx;
                  newLeft = fixLeft(newLeft);
                  mMainContainer.layout(newLeft, 0, newLeft + mSlideWidth, mSlideHeight);
              }

              // 处理移动事件
              performSlideEvent();
          }


### 五、使用教程
* 布局文件中

          <com.pinger.slide.SlideLayout
              xmlns:android="http://schemas.android.com/apk/res/android"
              android:id="@+id/slideLayout"
              android:layout_width="match_parent"
              android:background="@mipmap/icon_bg"
              android:layout_height="match_parent">

              // 菜单容器
              <include layout="@layout/layout_menu"/>

              // 主容器
              <include layout="@layout/layout_main"/>
          </com.pinger.slide.SlideLayout>

* 代码中获取对象，设置监听，设置打开或者关闭侧滑

### 六、总结
> 有了ViewDragHelper这个辅助类，对ViewGroup进行操作相对来说已经比较简单了，只需要处理计算和绘制的工作，其他的都已经做好了。当然ViewDragHelper的作用远不于此，想要了解更多的同学可以去研究一下这个类的源码。这里也只是简单的实现了侧滑功能，要想做的更完美的同学请自行修改。


[我的主页](http://www.jianshu.com/u/64f479a1cef7)
[项目下载](https://github.com/PingerOne/SlideLayout)
