package com.github.krystianmuchla.mnemo.id

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.krystianmuchla.mnemo.databinding.SignInViewBinding

class SignInDialogFragment : DialogFragment() {
    companion object {
        private const val TAG = "SignInDialog"
        private const val REQUEST_KEY = "request_key"

        fun newInstance(requestKey: String): SignInDialogFragment {
            return SignInDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(REQUEST_KEY, requestKey)
                }
            }
        }
    }

    private lateinit var requestKey: String
    private lateinit var view: SignInViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestKey = requireArguments().getString(REQUEST_KEY)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = SignInViewBinding.inflate(inflater)
        view.signIn.setOnClickListener {
            super.dismiss()
            val login = view.login.text.toString()
            val password = view.password.text.toString()
            val bundle = Bundle()
            bundle.putString("login", login)
            bundle.putString("password", password)
            parentFragmentManager.setFragmentResult(requestKey, bundle)
        }
        return view.root
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, TAG)
    }
}
