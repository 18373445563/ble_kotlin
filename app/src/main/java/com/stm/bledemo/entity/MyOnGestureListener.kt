package com.stm.bledemo.entity

import android.view.GestureDetector
import android.view.MotionEvent
import timber.log.Timber

class MyOnGestureListener : GestureDetector.SimpleOnGestureListener() {
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Timber.e("MyOnGestureListener!！！1" )
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        Timber.e("MyOnGestureListener!！！2" )
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Timber.e("MyOnGestureListener!！！3" )
        return false
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Timber.e("MyOnGestureListener!！！4" )
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        Timber.e("MyOnGestureListener!！！5" )
    }

    override fun onDown(e: MotionEvent): Boolean {
        Timber.e("MyOnGestureListener!！！6" )
        return false
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        Timber.e("MyOnGestureListener!！！7" )
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        Timber.e("MyOnGestureListener!！！8" )
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        Timber.e("MyOnGestureListener!！！9" )
        return false
    }

}