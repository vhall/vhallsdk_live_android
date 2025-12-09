package com.vhall.uimodule.login


import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.vhall.business.VhallSDK
import com.vhall.business.data.UserInfo
import com.vhall.business.data.source.UserInfoDataSource.UserInfoCallback
import com.vhall.uimodule.BuildConfig
import com.vhall.uimodule.R
import com.vhall.uimodule.base.BaseActivity
import com.vhall.uimodule.dao.UserDataStore
import com.vhall.uimodule.databinding.ActivityLoginBinding
import com.vhall.uimodule.main.MainActivity
import com.vhall.uimodule.utils.CommonUtil
import com.vhall.uimodule.widget.MainListPop
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {

    var account = ""
    var passWord = ""
    var id = ""
    var name = ""
    var url = ""

    override fun initView() {
        getData()
        mViewBinding.tvVersion.text = BuildConfig.VH_VERSION_NAME+"("+BuildConfig.BuildTime+")"
        mViewBinding.rgLogin.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.account_login) setAccountData()
            else if (checkedId == R.id.id_login) setThirdData()
        }
        mViewBinding.tvJoin.setOnClickListener { loginClick() }
        mViewBinding.tvSign.setOnClickListener { SignConfigActivity.startActivity(mContext) }
        mViewBinding.ivQ.setOnClickListener { showQ() }
        setThirdData()
//        CommonUtil.isGrantedAndRequestTelPermission(this,0)
    }


    private fun loginClick() = if (mViewBinding.accountLogin.isChecked) {
        loginByAccount()
    } else {
        loginByThirdId()
    }

    private fun showQ() = if (mViewBinding.accountLogin.isChecked) {
        MainListPop(mContext, "如何获取三方ID", "三方用户ID是用来关联微吼和外部用户，主播使用的三方用户ID请参考创建直播账号，third_user_id为三方用户ID，pass即为密码").show(mViewBinding.ivQ, 0, 0)
    } else {
        MainListPop(mContext, "如何获取三方ID", "三方用户ID是用来关联微吼和外部用户，您可以选择自己随机设置一串符，点击登录后微吼会自动生成一个对应的用户身份，可以调用OpenAPI接口创建一个三方用户后使用接口中third_user_id来此登录。特别注意头像和昵称只可设置一次且不可修改").show(mViewBinding.ivQ, 0, 0)
    }

    private fun loginByAccount() {
        val text = mViewBinding.edAccount.text.toString()
        val text1 = mViewBinding.edPassword.text.toString()
        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(text1)) {
            VhallSDK.loginByAccount(text, text1, LoginCallbackInternal())
        }
    }

    private fun loginByThirdId() {
        if (!TextUtils.isEmpty(mViewBinding.edAccount.text)) {
            VhallSDK.loginByThirdId(
                mViewBinding.edAccount.text.toString(),
                mViewBinding.edPassword.text.toString(),
                mViewBinding.edHead.text.toString(),
                LoginCallbackInternal()
            )
        }
    }

    private fun getData() {
        lifecycleScope.launch {
            account = UserDataStore.getAccount(mContext)
            passWord = UserDataStore.getPassword(mContext)
            id = UserDataStore.getThirdId(mContext)
            name = UserDataStore.getThirdName(mContext)
            url = UserDataStore.getThirdPic(mContext)
            mViewBinding.edAccount.setText(account)
            mViewBinding.edPassword.setText(passWord)
        }
    }

    private fun saveData() {
        lifecycleScope.launch {
            if (mViewBinding.accountLogin.isChecked()) {
                UserDataStore.saveAccount(
                    mViewBinding.edAccount.text.toString(),
                    mViewBinding.edPassword.text.toString(),
                    mContext
                )
            } else {
                UserDataStore.saveThirdId(
                    mViewBinding.edAccount.text.toString(),
                    mViewBinding.edPassword.text.toString(),
                    mViewBinding.edHead.text.toString(),
                    mContext
                )
            }
        }
    }

    private fun setAccountData() {
        id = mViewBinding.edAccount.text.toString()
        name = mViewBinding.edPassword.text.toString()
        url = mViewBinding.edHead.text.toString()

        mViewBinding.edHead.visibility = View.GONE
        mViewBinding.edAccount.setText(account)
        mViewBinding.edPassword.setText(passWord)
        mViewBinding.edAccount.hint = "账号"
        mViewBinding.edPassword.hint = "密码"
    }

    private fun setThirdData() {
        account = mViewBinding.edAccount.text.toString()
        passWord = mViewBinding.edPassword.text.toString()
        mViewBinding.edHead.visibility = View.VISIBLE
        mViewBinding.edAccount.setText(id)
        mViewBinding.edPassword.setText(name)
        mViewBinding.edHead.setText(url)
        mViewBinding.edAccount.hint = "三方用户ID"
        mViewBinding.edPassword.hint = getString(R.string.app_login_nickname)
    }

    inner class LoginCallbackInternal : UserInfoCallback {

        override fun onSuccess(userInfo: UserInfo) {
            saveData()
            startActivity(Intent(mContext, MainActivity::class.java))
            finish()
        }

        override fun onError(errorCode: Int, reason: String) {
            showToast(reason)
        }
    }

}