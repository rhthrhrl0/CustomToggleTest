package com.example.customtoggletest

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.databinding.BindingAdapter
import kotlin.math.abs
import kotlin.math.roundToInt

class ToggleCustom @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatImageView(context, attrs) {
    init {
        isSelected = false
        stateUpdate() //화면구성.
        //초깃값은 isDisabled는 따로 설정한 값, isSelected은 false기준으로.
    }

    //활성화유무 변수
    var isDisabled: Boolean = false
        set(isDisabled) {
            if (field != isDisabled) { //isDisabled가 값이 바뀐다면 클릭가능성유무도 갱신
                this.isClickable = isDisabled
            }
            field = isDisabled
            stateUpdate() //isDisabled프로퍼티가 변경될때마다 그에 맞는 화면을 보여줌.
        }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        stateUpdate() //이게 있어야 뷰모델의 데이터를 다시 받아올때 화면 갱신 가능.
    }

    //isSelceted는 이미 View에 존재하는 프로퍼티.
    private var mListener: OnCheckedChangeLister? = null

    interface OnCheckedChangeLister {
        fun onChecked(buttonView: ToggleCustom, isChecked: Boolean)
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeLister?) {
        mListener = listener
    }

    fun setOnCheckedChangeListener(listener: (ToggleCustom, Boolean) -> Unit) {
        mListener = object : OnCheckedChangeLister {
            override fun onChecked(buttonView: ToggleCustom, isChecked: Boolean) {
                listener(buttonView, isChecked)
            }
        }
    }


    private var downX=0f
    private var downY=0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX=event.x
                downY=event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDisabled) { //활성화가 된 상태라면
                    //만약 범위를 벗어난다면
                    if(abs(downX-event.x)>context.dpToPx(WIDTH)
                        || abs(downY-event.y)>context.dpToPx(HEIGHT)) {
                        downX=0f
                        downY=0f
                    }
                    else{
                        stateUpdateAnimation()//화면UI변경
                        mListener?.onChecked(this, isSelected) //사용자가 요청한 내용을 실행
                        isSelected = !isSelected  //isSelected상태를 변경
                    }
                }
                //비활성화상태이면 아무 변화도 주지 않음. 애초에 isClickable이 false라서
                //이벤트를 받지도 못함.
                else{
                    downX=0f
                    downY=0f
                }
                return true  //이 뷰에서 처리했음을 알림.
            }
            else -> {
                return true
            }
        }
    }

    fun stateUpdateAnimation() {
        if (!isDisabled && !isSelected) { //활성화되어있고 선택되지 않은 상태에서 애니메이션
            setImageResource(R.drawable.play_f_to_t)
            //setImageResource(R.drawable.play_f_to_t) //이것도 가능함.
            val avdFtoT: AnimatedVectorDrawable = this.drawable as AnimatedVectorDrawable
            avdFtoT.start()
        }
        if (!isDisabled && isSelected) { //활성화되어있고 선택된 상태에서의 애니메이션
            setImageResource(R.drawable.play_t_to_f)
            val avdFtoT: AnimatedVectorDrawable = this.drawable as AnimatedVectorDrawable
            avdFtoT.start()
        }
        //활성화 되어있지 않은 상태에서는 아무 것도 없음.
    }

    //지정한 상태에 맞게 화면에 보일 이미지를 초기화하는 코드
    fun stateUpdate() {
        if (!isDisabled && !isSelected) {
            setImageResource(R.drawable.disabled_f_selected_f)
        } else if (!isDisabled && isSelected) {
            setImageResource(R.drawable.disabled_f_selected_t)
        } else if (isDisabled && !isSelected) {
            setImageResource(R.drawable.disabled_t_selected_f)
        } else if (isDisabled && isSelected) {
            setImageResource(R.drawable.disabled_t_selected_t)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = context.dpToIntPx(WIDTH) //실수를
        val height = context.dpToIntPx(HEIGHT)
        setMeasuredDimension(width, height)
    }

    companion object {
        //해당 커스텀뷰의 가로세로 크기를 상수로 만들기. 나중에 활용함
        private const val WIDTH = 51f
        private const val HEIGHT = 31f

        //BindingAdapter("XML에서 쓸 속성명") 어노테이션은
        //데이터바인딩시키고 싶은 원하는 속성을 setter로 지정 가능함.
        @JvmStatic
        @BindingAdapter("isDisabled")
        fun setDisabled(toggle: ToggleCustom, isDisabled: Boolean) {
            toggle.isDisabled = isDisabled
        }

        @JvmStatic
        @BindingAdapter("isSelected")
        fun setSelected(toggle: ToggleCustom, isSelected: Boolean) {
            toggle.isSelected = isSelected
        }
    }

    //확장함수. 얘네는 YDS-An
    infix fun Context.dpToIntPx(dp: Float): Int {
        val displayMetrics = this.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
            .roundToInt()
    }
    infix fun Context.dpToPx(dp: Float): Float {
        val displayMetrics = this.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
    }
}