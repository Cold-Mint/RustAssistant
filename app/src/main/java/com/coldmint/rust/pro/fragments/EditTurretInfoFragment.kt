package com.coldmint.rust.pro.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coldmint.rust.core.turret.CoordinateData
import com.coldmint.rust.core.turret.TurretData
import com.coldmint.rust.core.turret.TurretView
import com.coldmint.rust.pro.R
import com.coldmint.rust.pro.base.BaseFragment
import com.coldmint.rust.pro.databinding.FragmentEditTurretInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 编辑炮塔信息碎片
 * @property fragmentEditTurretInfoBinding FragmentEditTurretInfoBinding
 */
class EditTurretInfoFragment(val turretView: TurretView) : BottomSheetDialogFragment() {
    private lateinit var fragmentEditTurretInfoBinding: FragmentEditTurretInfoBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentEditTurretInfoBinding =
            FragmentEditTurretInfoBinding.inflate(layoutInflater, container, false)
        return fragmentEditTurretInfoBinding.root
    }

    fun enableButton() {
        val x = fragmentEditTurretInfoBinding.xInputEditText.text.toString()
        val y = fragmentEditTurretInfoBinding.yInputEditText.text.toString()
        fragmentEditTurretInfoBinding.saveButton.isEnabled = !(x.isBlank() || y.isBlank())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentEditTurretInfoBinding.xInputEditText.setText(turretView.getTurretData()!!.gameCoordinateData.x.toString())
        fragmentEditTurretInfoBinding.yInputEditText.setText(turretView.getTurretData()!!.gameCoordinateData.y.toString())
        fragmentEditTurretInfoBinding.xInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isBlank()) {
                    fragmentEditTurretInfoBinding.xInputEditLayout.error =
                        getString(R.string.please_enter_the_x_coordinate)
                } else {
                    fragmentEditTurretInfoBinding.xInputEditLayout.isErrorEnabled = false
                }
                enableButton()
            }
        })
        fragmentEditTurretInfoBinding.yInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isBlank()) {
                    fragmentEditTurretInfoBinding.yInputEditLayout.error =
                        getString(R.string.please_enter_the_y_coordinate)
                } else {
                    fragmentEditTurretInfoBinding.yInputEditLayout.isErrorEnabled = false
                }
                enableButton()
            }
        })
        fragmentEditTurretInfoBinding.saveButton.setOnClickListener {
            val x = fragmentEditTurretInfoBinding.xInputEditText.text.toString().toInt()
            val y = fragmentEditTurretInfoBinding.yInputEditText.text.toString().toInt()
            turretView.setGameCoordinateData(CoordinateData(x, y))

            dialog?.dismiss()
        }
    }
}