package com.alfresco.content.app.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.alfresco.content.app.R
import com.google.android.material.button.MaterialButton

class AccountPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {
    private lateinit var signOutButton: MaterialButton
    private lateinit var parentUserInfo: LinearLayout
    var onSignOutClickListener: View.OnClickListener? = null
    var accessibilityTextUserInfo = ""

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        signOutButton = holder.findViewById(R.id.sign_out_button) as MaterialButton
        parentUserInfo = holder.findViewById(R.id.parent_user_info) as LinearLayout
        parentUserInfo.contentDescription = accessibilityTextUserInfo
        signOutButton.setOnClickListener(onSignOutClickListener)
    }
}
