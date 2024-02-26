package ru.flocator.map.internal.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.imageview.ShapeableImageView
import ru.flocator.core.extensions.findDrawable
import ru.flocator.core.utils.ViewUtils.dpToPx
import ru.flocator.map.R
import ru.flocator.map.internal.data.bitmap_creator.BitmapCreator
import ru.flocator.map.internal.data.view.ReusableView
import ru.flocator.map.internal.data.view.ViewFactory

@SuppressLint("ViewConstructor")
internal class UserView constructor(
    context: Context,
    private val onRecycle: () -> Unit
) : FrameLayout(context), BitmapCreator, ReusableView {
    // Default values for measurement
    private val diameter: Int = dpToPx(48, context)
    private val textViewHeight = dpToPx(16, context)
    private val textSize = 10f
    private val padding = dpToPx(2, context)

    // Calculated objects
    private val userAvatarFrameLayout: FrameLayout
    private val userAvatarImageView: ShapeableImageView
    private val userNameTextView: TextView

    @Volatile
    override var isBusy: Boolean = false

    private var isTargetUser: Boolean = false

    private var _avatarUri: String? = null
    val avatarUri: String?
        get() = _avatarUri

    init {

        // User Image
        LayoutInflater.from(context).inflate(R.layout.round_image_view, this, true)
        userAvatarFrameLayout = findViewById(R.id.layout)
        userAvatarImageView = findViewById(R.id.image)

        setTargetUser(isTargetUser)

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
                ru.flocator.design.R.color.black_alpha_50,
                null
            )
        )
        userNameTextView.setTextColor(ResourcesCompat.getColor(resources, ru.flocator.design.R.color.white, null))
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
        layout(0, 0, diameter, diameter)

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

    fun setTargetUser(isTargetUser: Boolean) {
        this.isTargetUser = isTargetUser
        with(resources) {
            if (isTargetUser) {
                userAvatarFrameLayout.background = findDrawable(R.drawable.user_circle_bg)
            } else {
                userAvatarFrameLayout.background = findDrawable(R.drawable.friend_circle_bg)
            }
        }
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
                ru.flocator.design.R.drawable.base_avatar_image,
                null
            )
        )
    }

    fun setUserName(userName: String) {
        userNameTextView.text = userName
    }

    override fun recycle() {
        super.recycle()
        onRecycle.invoke()
    }

    class Factory(private var context: Context?) : ViewFactory<UserView> {

        override fun create(onRecycle: () -> Unit): UserView {
            if (context == null) {
                throw IllegalStateException("Factory is already cleared!")
            }
            return UserView(context!!, onRecycle)
        }

        override fun onClear() {
            context = null
        }
    }
}