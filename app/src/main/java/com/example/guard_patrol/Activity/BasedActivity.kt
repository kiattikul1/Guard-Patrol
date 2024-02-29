import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.L
import com.example.guard_patrol.Data.Dialog.LoadingDialog
import com.example.guard_patrol.Data.Preference.TokenPref
import com.example.guard_patrol.Data.Preference.WorkspacePref
import com.example.guard_patrol.R

open class BasedActivity: AppCompatActivity() {
    internal lateinit var tokenPreference: TokenPref
    internal lateinit var workspacePreference: WorkspacePref
    internal lateinit var loadingDialog: LoadingDialog
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         tokenPreference = TokenPref(this)
         workspacePreference = WorkspacePref(this)
         loadingDialog = LoadingDialog(this)
    }

}


