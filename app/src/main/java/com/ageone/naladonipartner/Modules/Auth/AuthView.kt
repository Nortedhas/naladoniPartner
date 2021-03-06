package com.ageone.naladonipartner.Modules.Auth

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import com.ageone.naladonipartner.External.Base.ConstraintLayout.dismissFocus
import com.ageone.naladonipartner.R
import com.ageone.naladonipartner.External.Base.Module.BaseModule
import com.ageone.naladonipartner.External.Base.RecyclerView.BaseAdapter
import com.ageone.naladonipartner.External.Base.RecyclerView.BaseViewHolder
import com.ageone.naladonipartner.External.InitModuleUI
import com.ageone.naladonipartner.External.Libraries.Alert.alertManager
import com.ageone.naladonipartner.External.Libraries.Alert.single
import com.ageone.naladonipartner.External.RxBus.RxBus
import com.ageone.naladonipartner.External.RxBus.RxEvent
import com.ageone.naladonipartner.Models.User.user
import com.ageone.naladonipartner.Modules.Auth.rows.AuthButtonViewHolder
import com.ageone.naladonipartner.Modules.Auth.rows.AuthTextInputViewHolder
import com.ageone.naladonipartner.Modules.Auth.rows.AuthTextViewHolder
import com.ageone.naladonipartner.Modules.Auth.rows.initialize
import timber.log.Timber
import yummypets.com.stevia.*

class AuthView(initModuleUI: InitModuleUI = InitModuleUI()) : BaseModule(initModuleUI) {

    val viewModel = AuthViewModel()

    val viewAdapter by lazy {
        val viewAdapter = Factory(this)
        viewAdapter
    }

    init {
//        viewModel.loadRealmData()

        setBackgroundResource(R.drawable.base_background)

        toolbar.title = "Авторизация"
        toolbar.textColor = Color.WHITE
        toolbar.textView.textSize = 28F

        renderToolbar()

        bodyTable.adapter = viewAdapter
        bodyTable.overScrollMode = View.OVER_SCROLL_NEVER


        renderUIO()
        bindUI()
    }

    fun bindUI() {
        /*compositeDisposable.add(
            RxBus.listen(RxEvent.Event::class.java).subscribe {//TODO: change type event
                bodyTable.adapter?.notifyDataSetChanged()
            }
        )*/
    }

    inner class Factory(val rootModule: BaseModule) : BaseAdapter<BaseViewHolder>() {

        private val AuthTextType = 0
        private val AuthTextInputType = 1
        private val AuthTextButtonType = 2

        override fun getItemCount() = 3//viewModel.realmData.size

        override fun getItemViewType(position: Int): Int = when (position) {
            0 -> AuthTextType
            1 -> AuthTextInputType
            2 -> AuthTextButtonType
            else -> -1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

            val layout = ConstraintLayout(parent.context)

            layout
                .width(matchParent)
                .height(wrapContent)

            val holder = when (viewType) {
                AuthTextType -> {
                    AuthTextViewHolder(layout)
                }
                AuthTextInputType -> {
                    AuthTextInputViewHolder(layout)
                }
                AuthTextButtonType -> {
                    AuthButtonViewHolder(layout)
                }
                else -> {
                    BaseViewHolder(layout)
                }
            }

            return holder
        }

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {

            when (holder) {
                is AuthTextViewHolder -> {
                    holder.initialize()
                }
                is AuthTextInputViewHolder -> {
                    holder.initialize("Введите код-идентификатор")
                    holder.textInputAuth.editText?.doOnTextChanged { text, start, count, after ->
                        viewModel.model.code = text.toString()
                    }
                    innerContent.dismissFocus(holder.textInputAuth.editText)
                }
                is AuthButtonViewHolder -> {
                    holder.initialize()
                    holder.buttonAuth.setOnClickListener {
                        viewModel.validate {
                            user.isAuthorized = true
                            rootModule.emitEvent?.invoke(AuthViewModel.EventType.OnNextPressed.name)
                        }
                        /*if(viewModel.model.code.count() < 10 ){
                            alertManager.single("Ошибка", "Неверный код", null, "Понятно"){
                                _, position ->
                                when(position){
                                    0 -> Timber.i("Dismiss alert manager")
                                }
                            }
                        }else {
                            }*/
                    }
                }
            }
        }
    }
}

fun AuthView.renderUIO() {
    renderBodyTable()
}


