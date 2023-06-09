package ru.flocator.app.map.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.flocator.R
import ru.flocator.app.map.ui.BitmapCreator
import ru.flocator.app.common.utils.ViewUtils.dpToPx
import com.google.android.material.imageview.ShapeableImageView


class UserView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val isTargetUser: Boolean = false
) : FrameLayout(context, attrs, defStyleAttr), BitmapCreator {
    // Default values for measurement
    private val defaultDiameter = dpToPx(48, context)
    private val textViewHeight = dpToPx(16, context)
    private val textSize = 10f
    private val padding = dpToPx(2, context)

    // Calculated objects
    private val diameter: Int
    private val userAvatarFrameLayout: FrameLayout
    private val userAvatarImageView: ShapeableImageView
    private val userNameTextView: TextView

    private var _avatarUri: String? = null
    val avatarUri: String?
        get() = _avatarUri

    init {
        if (attrs != null) {
            val typedAttrs =
                context.obtainStyledAttributes(attrs, R.styleable.UserView, defStyleAttr, 0)
            diameter =
                typedAttrs.getDimension(
                    R.styleable.UserView_diameter,
                    defaultDiameter.toFloat()
                )
                    .toInt()
            typedAttrs.recycle()
        } else {
            diameter = defaultDiameter
        }

        // User Image
        LayoutInflater.from(context).inflate(R.layout.round_image_view, this, true)
        userAvatarFrameLayout = findViewById(R.id.layout)
        userAvatarImageView = findViewById(R.id.image)

        if (isTargetUser) {
            userAvatarFrameLayout.background =
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.user_circle_bg,
                    null
                )!!

        }

        setAvatarPlaceHolder()

        // User Name
        userNameTextView = TextView(context)

        userNameTextView.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, textViewHeight)
        userNameTextView.textSize = textSize
        userNameTextView.setPadding(2, 0, 2, 0)
        userNameTextView.gravity = Gravity.CENTER
        userNameTextView.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.black_alpha_50,
                null
            )
        )
        userNameTextView.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        userNameTextView.maxLines = 1
        userNameTextView.ellipsize = TextUtils.TruncateAt.END

        addView(userNameTextView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        userAvatarFrameLayout.measure(
            MeasureSpec.makeMeasureSpec(diameter, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(diameter, MeasureSpec.EXACTLY)
        )
        userNameTextView.measure(
            MeasureSpec.makeMeasureSpec(diameter, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(textViewHeight, MeasureSpec.EXACTLY)
        )
        setMeasuredDimension(
            diameter,
            diameter + padding + textViewHeight
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        userAvatarFrameLayout.layout(0, 0, diameter, diameter)
        userNameTextView.layout(
            0,
            diameter + padding,
            diameter,
            diameter + padding + textViewHeight
        )
    }

    override fun createBitmap(): Bitmap {
        measure(0, 0)
        layout(0, 0, defaultDiameter, defaultDiameter)

        // Create a bitmap with the dimensions of the custom view
        val bitmap = Bitmap.createBitmap(
            measuredWidth,
            measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        // Create a canvas with the bitmap
        val canvas = Canvas(bitmap)

        // Draw the custom view onto the canvas
        draw(canvas)

        return bitmap
    }

    fun setAvatarBitmap(bitmap: Bitmap, uri: String?) {
        if (_avatarUri != uri) {
            if (uri == null) {
                setAvatarPlaceHolder()
            } else {
                userAvatarImageView.setImageBitmap(bitmap)
            }
            _avatarUri = uri
        }
    }

    fun setAvatarPlaceHolder() {
        userAvatarImageView.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.base_avatar_image,
                null
            )
        )
    }

    fun setUserName(userName: String) {
        userNameTextView.text = userName
    }
}