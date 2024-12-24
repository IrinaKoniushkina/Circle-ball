package com.example.coloredcircles

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt
import kotlin.random.Random

data class Circle(var x: Float, var y: Float, var radius: Float, var color: Int, var dragging: Boolean = false) {
    fun contains(touchX: Float, touchY: Float): Boolean {
        val dx = touchX - x
        val dy = touchY - y
        return sqrt(dx * dx + dy * dy) <= radius
    }
}
class GameView(ctx: Context) : View(ctx) {
    var h = 1000; var w = 1000
    val paint = Paint()
    val random = Random(System.currentTimeMillis())
    val radius = 50f
    var targetCircleIndex: Int = 0
    var circles: MutableList<Circle> = mutableListOf()
    var gameOver = false
    var dragCircle: Circle? = null
    init {
        generateCircles(5)
    }
    private fun generateCircles(numCircles: Int) {
        circles.clear()
        for (i in 1..numCircles) {
            var x: Float
            var y: Float
            do {
                x = random.nextFloat() * (w - 2 * radius) + radius
                y = random.nextFloat() * (h - 100 - 2 * radius) + radius // Изменено, чтобы кружки не перекрывали "лузу"
            } while (circles.any { sqrt((x - it.x) * (x - it.x) + (y - it.y) * (y - it.y)) < 2 * radius })
            circles.add(Circle(x, y, radius, Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))))
        }
    }
    private fun isInsideTarget(circle: Circle): Boolean {
        val targetRectY = h - 100f
        return circle.x >= 0 && circle.x <= w && circle.y >= targetRectY && circle.y <= targetRectY + 100
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        h = bottom - top
        w = right - left
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.LTGRAY)
        paint.style = Paint.Style.FILL
        circles.forEach { circle ->
            paint.color = circle.color
            canvas.drawCircle(circle.x, circle.y, radius, paint)
        }
        val targetRectY = h - 100f
        paint.color = circles[targetCircleIndex].color
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, targetRectY, w.toFloat(), targetRectY + 100f, paint)
        if (gameOver) {
            paint.color = Color.RED
            paint.textSize = 50f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Game Over!", w / 2f, h / 2f, paint)
        }
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val touchX = it.x
            val touchY = it.y
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    circles.forEach { circle ->
                        if (circle.contains(touchX, touchY)) {
                            dragCircle = circle
                            dragCircle?.dragging = true
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    dragCircle?.let { circle ->
                        circle.x = touchX
                        circle.y = touchY
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    dragCircle?.let { circle ->
                        if (isInsideTarget(circle)) {
                            circles.remove(circle)
                            if (circles.isEmpty()) {
                                gameOver = true
                            } else {
                                targetCircleIndex = (targetCircleIndex + 1) % circles.size
                            }
                        }
                        dragCircle?.dragging = false
                        dragCircle = null
                        invalidate()
                    }
                }
                else -> {}
            }
        }
        return true
    }
}