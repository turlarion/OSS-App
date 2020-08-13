package ru.mpei.vmss.myapplication.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_profile.*
import org.json.JSONException
import org.json.JSONObject
import ru.mpei.vmss.myapplication.R
import ru.mpei.vmss.myapplication.activities.MainActivity
import ru.mpei.vmss.myapplication.activities.MainActivity.Companion.deleteData
import ru.mpei.vmss.myapplication.activities.TasksActivity

class Profile : Fragment {
    private var hashPass: String? = null
    private var userId: String? = null
    private var userName: String? = null
    private var userSurname: String? = null
    private var userCapital: String? = null

    constructor()

    constructor(pass: String?, id: String?) {
        hashPass = pass
        userId = id
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        getUserData(profileName, profileCoins)
        profileExitButton.setOnClickListener {
            MainActivity.MyAdapter.tasksFragment.updateList()
            deleteData()
            User.updateLayout()
        }
        profileShopButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(requireContext().getString(R.string.shopUrl)))
            startActivity(browserIntent)
        }
        profileTaskTypeList.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, id: Long ->
            val intent = Intent(context, TasksActivity::class.java)
            intent.putExtra("type", id)
            startActivity(intent)
        }
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    private fun getUserData(name: TextView, capital: TextView) {
        try {
            val body = JSONObject()
            body.put("id", userId)
            body.put("pass", hashPass)
            val request = JsonObjectRequest(Request.Method.POST, context!!.getString(R.string.lkUrl), body,
                    Response.Listener { response: JSONObject ->
                        val result = response.optBoolean("error")
                        if (!result) {
                            userName = response.optString("name")
                            userSurname = response.optString("surname")
                            userCapital = response.optString("capital")
                            updateInfo(name, capital)
                        } else {
                            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { Toast.makeText(context, "Возникла проблема, попробуйте позже", Toast.LENGTH_SHORT).show() })
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInfo(name: TextView, capital: TextView) {
        name.text = "$userName $userSurname"
        capital.text = "Ваш баланс: $userCapital р."
    }
}