import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.guard_patrol.Data.Preference.TokenPref
import com.example.guard_patrol.Data.Preference.WorkspacePref

open class BasedActivity: AppCompatActivity() {
    internal lateinit var tokenPreference: TokenPref
    internal lateinit var workspacePreference: WorkspacePref
    internal lateinit var loadingDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenPreference = TokenPref(this)
        workspacePreference = WorkspacePref(this)
    }
}


