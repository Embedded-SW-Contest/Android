package com.uwb.safety.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.uwb.safety.src.MainActivity
import com.uwb.safety.databinding.DialogAlarmBinding

class CustomDialog (confirmDialogInterface: ConfirmDialogInterface, id: Int
) : DialogFragment() {

    // 뷰 바인딩 정의
    private var _binding: DialogAlarmBinding? = null
    private val binding get() = _binding!!

    private var confirmDialogInterface: ConfirmDialogInterface? = null
    private lateinit var alarm :Alarm
    private var id: Int? = null

    init {
        this.id = id
        this.confirmDialogInterface = confirmDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAlarmBinding.inflate(inflater, container, false)
        val view = binding.root
        alarm = Alarm(confirmDialogInterface as MainActivity) // alarm 객체 생성
        // 레이아웃 배경을 투명하게 해줌, 필수 아님
        //dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alarm.ringAlarm()
        // 취소 버튼 클릭
        binding.alarmDialog.setOnClickListener {
            this.confirmDialogInterface?.onDialogClick(id!!)
            dismiss()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그의 크기를 MATCH_PARENT로 설정
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경을 투명하게 설정
    }

    override fun onDestroyView() {
        alarm.stopAlarm()
        super.onDestroyView()
        _binding = null
    }
}

interface ConfirmDialogInterface {
    fun onDialogClick(id: Int)
}