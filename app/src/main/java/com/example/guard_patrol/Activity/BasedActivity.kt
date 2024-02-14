import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.guard_patrol.Data.Preference.TokenPref
import com.example.guard_patrol.Data.Preference.WorkspacePref
import com.example.guard_patrol.R

open class BasedActivity: AppCompatActivity() {
    internal lateinit var tokenPreference: TokenPref
    internal lateinit var workspacePreference: WorkspacePref
    private var progressBar: ProgressBar? = null
    private lateinit var dialog : Dialog
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenPreference = TokenPref(this)
        workspacePreference = WorkspacePref(this)
    }

    fun showLoadingDialog(context: Context) {
        if (progressBar == null) {
            dialog = Dialog(context)
            dialog.apply {
                setCancelable(false)
                setContentView(R.layout.custom_dialog_loading)
                window?.setBackgroundDrawableResource(android.R.color.transparent);
                show()
            }
        }
        progressBar?.visibility = ProgressBar.VISIBLE
    }

    fun dismissLoadingDialog() {
        progressBar?.visibility = ProgressBar.GONE
        dialog.hide()
    }

}


