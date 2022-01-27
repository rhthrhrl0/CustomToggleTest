package com.example.customtoggletest

import android.content.Context
import android.content.res.TypedArray
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

    //isFirst는 해당 뷰가 화면 회전 등으로 새로 생성되는 경우 데이터바인딩으로
    //isDisabled와 isSelected가 초기화 될 때 isSelected는 stateUpdate()를 호출해야함
    //안그러면 isDisabled의 정보로만 화면이 갱신됨.
    //하지만 뷰 객체 생성시에 데이터바인딩으로 인한 첫 초기화가 아니라
    //그냥 토글 클릭으로 인한 isSelected 변경이라면 isSelected쪽에서는 stateUpdate()를 호출하면 안됨.
    //왜냐하면 그렇게 되면, 단순 토글클릭으로 인해 데이터바인딩쪽에서 isSelected가 바뀌게 될때마다
    //stateUpdate()를 호출하게 되면 애니메이션이 중간에 끊기게 됨. 그래서 애니메이션이 없는것처럼 보인 것임.
    //이것을 해결하기 위해 isFirst라는 변수를 추가함.
    var isFirst: Int

    //활성화유무 변수
    var isDisabled: Boolean = false
        set(isDisabled) {
            if (field != isDisabled) { //isDisabled가 값이 바뀐다면 클릭가능성유무도 갱신
                this.isClickable = isDisabled
            }
            field = isDisabled
            if (isFirst < 2) {
                stateUpdate() //isDisabled프로퍼티가 변경될때마다 그에 맞는 화면을 보여줌.
                isFirst++
            } else {
                stateUpdate() //isDisabled프로퍼티가 변경될때마다 그에 맞는 화면을 보여줌.
            }
        }

    init {
        Log.d("kmj", "셀렉트 init전: $isSelected")
        //isSelected는 굳이 false로 초기화 안해도 초깃값이 false임.
        stateUpdate() //화면구성.
        //초깃값은 isDisabled는 따로 설정한 값, isSelected은 false기준으로.
        val a:TypedArray=context.obtainStyledAttributes(attrs,R.styleable.ToggleCustom,0,0)
        isSelected=a.getBoolean(R.styleable.ToggleCustom_toggleSelected,false)
        isDisabled=a.getBoolean(R.styleable.ToggleCustom_toggleDisabled,false)
        isFirst = 0
    }


    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (isFirst < 2) {
            //애니메이션이 도는 중간에 118번째 줄 코드로 인해 해당 setSelected가 실행되면 애니메이션이 끊기게 됨.
            stateUpdate() //이게 있어야 데이터바인딩으로 데이터를 다시 받아올때 알맞은 화면 갱신 가능.
            //만약 stateUpdate가 없다면 데이터바인딩으로 값을 초기화할때 만약 isSelected가 isDisabled보다
            //xml파일 기준으로 아래에 선언되어 있어서 isSelected 초기화가 늦을 경우 isSelected의 정보가
            //상관없이 오직 isDisabled의 정보에 의해서만 화면에 보여지게 된다.
            isFirst++
        }
        //isFirst가 2보다 크다면 isSelected가 변할때 stateUpdate()를 호출하면 안됨. 애니메이션이 끊기게 됨.
    }

    //만약 isDisabled랑 isSelected중 하나라도 데이터바인딩을 안시킨 상태로 해당 토글을 만들게 되면
    //isFirst가 2보다 작게 됨. 이경우에는 클릭으로인해 isSelected를 바꿀때 stateUpdate()를 호출하면 안됨.
    private fun setSelected(selected: Boolean, int: Int) {
        super.setSelected(selected)
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


    private var downX = 0f
    private var downY = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDisabled) { //활성화가 된 상태라면
                    //만약 범위를 벗어난다면
                    if (abs(downX - event.x) > context.dpToPx(WIDTH)
                        || abs(downY - event.y) > context.dpToPx(HEIGHT)
                    ) {
                        downX = 0f
                        downY = 0f
                    } else {
                        stateUpdateAnimation()
                        mListener?.onChecked(this, isSelected) //사용자가 요청한 내용을 실행
                        if (isFirst >= 2) {
                            isSelected = !isSelected  //isSelected상태를 변경
                        } else {
                            setSelected(!isSelected, isFirst)
                        }

                    }
                }
                //비활성화상태이면 아무 변화도 주지 않음. 애초에 isClickable이 false라서
                //이벤트를 받지도 못함.
                else {
                    downX = 0f
                    downY = 0f
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
            val avdFtoT: AnimatedVectorDrawable = this.drawable as AnimatedVectorDrawable
            avdFtoT.start()
        }
        if (!isDisabled && isSelected) { //활성화되어있고 선택된 상태에서의 애니메이션
            setImageResource(R.drawable.play_t_to_f)
            val avdTtoF: AnimatedVectorDrawable = this.drawable as AnimatedVectorDrawable
            avdTtoF.start()
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

    //확장함수. 얘네는 YDS-Anroid에서 가져옴.
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