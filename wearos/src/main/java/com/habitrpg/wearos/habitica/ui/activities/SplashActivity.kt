package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.databinding.ActivitySplashBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    override val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        if (viewModel.hasAuthentication) {
            startMainActivity()
            return
        }

        viewModel.onLoginCompleted = {
            if (it) {
                startMainActivity()
            } else {
                startLoginActivity()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        messageClient.addListener(viewModel)

        if (!viewModel.hasAuthentication) {
            sendMessage("provide_auth", "/request/auth", null) {
                if (it) {
                    showAccountLoader(true)
                } else {
                    showAccountLoader(false)
                    startLoginActivity()
                }
            }
        }
    }

    override fun onStop() {
        messageClient.removeListener(viewModel)
        super.onStop()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showAccountLoader(show: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (show) {
                startAnimatingProgress()
            } else {
                stopAnimatingProgress()
            }
            binding.textView.isVisible = show
            delay(90.toDuration(DurationUnit.SECONDS))
            if (isActive) {
                // the sync attempt has timed out
                startLoginActivity()
            }
        }
    }
}
