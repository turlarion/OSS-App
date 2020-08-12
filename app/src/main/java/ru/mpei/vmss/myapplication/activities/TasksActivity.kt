package ru.mpei.vmss.myapplication.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ExpandableListView.OnGroupExpandListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ru.mpei.vmss.myapplication.R
import ru.mpei.vmss.myapplication.fragments.User
import ru.mpei.vmss.myapplication.pojo.Task
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TasksActivity : AppCompatActivity() {
    private lateinit var context: Context
    private var adapter: specialAdapter? = null
    private val dataList: MutableList<Task> = ArrayList()
    private var type = 0
    private lateinit var editReportImage: ImageView
    private var currentPhotoPath: String? = null
    private var dialog: AlertDialog? = null
    private lateinit var list: ExpandableListView
    private val TAKE_PHOTO = 0
    private val CHOOSE_PHOTO = 1
    private val TASKS_IN_PROCESS = 0
    private val TASKS_IN_CHECK = 1
    private val TASKS_FINISHED = 2
    private val TASKS_DECLINED = 3

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)
        context = Objects.requireNonNull(applicationContext)
        type = intent.getLongExtra("type", 0).toInt()
        val mToolbar = findViewById<Toolbar>(R.id.tasksTypeToolbar)
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        mToolbar.setNavigationOnClickListener { view: View? -> finish() }
        list = findViewById(R.id.tasksTypeList)
        list.setOnGroupExpandListener(OnGroupExpandListener { groupPosition: Int ->
            for (g in 0 until adapter!!.groupCount) {
                if (g != groupPosition) {
                    list.collapseGroup(g)
                }
            }
        })
        when (type) {
            TASKS_IN_PROCESS -> mToolbar.title = "ВЫПОЛНЯЕМЫЕ ЗАДАНИЯ"
            TASKS_IN_CHECK -> mToolbar.title = "ЗАДАНИЯ НА ПРОВЕРКЕ"
            TASKS_FINISHED -> mToolbar.title = "ЗАВЕРШЕННЫЕ ЗАДАНИЯ"
            TASKS_DECLINED -> mToolbar.title = "ОТКЛОНЕННЫЕ ЗАДАНИЯ"
        }
        adapter = specialAdapter(context, dataList, type)
        list.setAdapter(adapter)
        updateList(type)
        val refresher = findViewById<SwipeRefreshLayout>(R.id.tasksTypeRefresher)
        refresher.setColorSchemeColors(context.getColor(R.color.bgBottomNavigation))
        refresher.setOnRefreshListener {
            updateList(type)
            refresher.isRefreshing = false
        }
    }

    private fun updateList(type: Int) {
        dataList.clear()
        val body = JSONObject()
        try {
            body.put("id", User.userId)
            when (type) {
                1 -> body.put("type", "inCheck")
                2 -> body.put("type", "accepted")
                3 -> body.put("type", "refused")
                else -> body.put("type", "taken")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val request = JsonArrayRequest(Request.Method.POST,
                context.getString(R.string.getTasksUrl), body, Response.Listener { response: JSONArray ->
            for (i in 0 until response.length()) {
                var obj: JSONObject
                var line: Task
                try {
                    obj = response.getJSONObject(i)
                    line = Task(obj.getString("description_short"),
                            obj.getString("description_full"),
                            obj.getString("place"),
                            obj.getString("reward"),
                            obj.getString("start"),
                            obj.getString("end"),
                            obj.getString("refuse_before"))
                    if (type == 1) {
                        if (obj.getString("status") == "onchecking") {
                            line.status = "Проверка выполнения"
                        } else {
                            line.status = "Проверка штрафа"
                        }
                        line.changeBefore = obj.getString("changeBefore")
                    }
                    line.id = obj.getString("id_task")
                    line.penalty = obj.getString("penalty")
                    dataList.add(line)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            adapter!!.notifyDataSetChanged()
        },
                Response.ErrorListener { error: VolleyError? -> Toast.makeText(context, "Ошибка соединения", Toast.LENGTH_LONG).show() })
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

    @SuppressLint("SetTextI18n")
    fun showDialog(type: String?, task: Task) {
        val builder = AlertDialog.Builder(this@TasksActivity)
        when (type) {
            "finish" -> {
                val finishView = layoutInflater.inflate(R.layout.finish_dialog, null)
                val finishWithReport = finishView.findViewById<Button>(R.id.withReport)
                finishWithReport.setOnClickListener { v: View? ->
                    val body = JSONObject()
                    try {
                        body.put("user_id", User.userId)
                        body.put("task_id", task.id)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    val request = JsonArrayRequest(Request.Method.POST,
                            context!!.getString(R.string.confirmTaskUrl),
                            body,
                            Response.Listener { response: JSONArray? -> },
                            Response.ErrorListener { error: VolleyError? -> })
                    val requestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(request)
                    dialog!!.cancel()
                    closeAll()
                    updateList(TASKS_IN_PROCESS)
                    showDialog("editReport", task)
                }
                val finishNoReport = finishView.findViewById<Button>(R.id.noReport)
                finishNoReport.setOnClickListener { v: View? ->
                    val body = JSONObject()
                    try {
                        body.put("user_id", User.userId)
                        body.put("task_id", task.id)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    val request = JsonArrayRequest(Request.Method.POST,
                            context!!.getString(R.string.confirmTaskUrl),
                            body,
                            Response.Listener { response: JSONArray? -> },
                            Response.ErrorListener { error: VolleyError? -> })
                    val requestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(request)
                    dialog!!.cancel()
                    closeAll()
                    updateList(TASKS_IN_PROCESS)
                }
                builder.setView(finishView)
            }
            "refuseNormal" -> {
                val refuseNormalView = layoutInflater.inflate(R.layout.decline_normal_dialog, null)
                val refuseNormal = refuseNormalView.findViewById<Button>(R.id.declineNormalButton)
                refuseNormal.setOnClickListener { v: View? ->
                    val body = JSONObject()
                    try {
                        body.put("user_id", User.userId)
                        body.put("task_id", task.id)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    val request = JsonArrayRequest(Request.Method.POST,
                            context!!.getString(R.string.declineTaskUrl),
                            body,
                            Response.Listener { response: JSONArray? -> },
                            Response.ErrorListener { error: VolleyError? -> })
                    val requestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(request)
                    dialog!!.cancel()
                    closeAll()
                    updateList(TASKS_IN_PROCESS)
                }
                builder.setView(refuseNormalView)
            }
            "refusePenalty" -> {
                val refusePenaltyView = layoutInflater.inflate(R.layout.decline_penalty_dialog, null)
                val penalty = refusePenaltyView.findViewById<TextView>(R.id.declinePenalty)
                val refuseWithReason = refusePenaltyView.findViewById<Button>(R.id.declinePenaltyReason)
                refuseWithReason.setOnClickListener { v: View? ->
                    val body = JSONObject()
                    try {
                        body.put("user_id", User.userId)
                        body.put("task_id", task.id)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    val request = JsonArrayRequest(Request.Method.POST,
                            context!!.getString(R.string.declineTaskUrl),
                            body,
                            Response.Listener { response: JSONArray? -> },
                            Response.ErrorListener { error: VolleyError? -> })
                    val requestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(request)
                    dialog!!.cancel()
                    closeAll()
                    updateList(TASKS_IN_PROCESS)
                    showDialog("editReport", task)
                }
                val refuseNoReason = refusePenaltyView.findViewById<Button>(R.id.declinePenaltyNoReason)
                refuseNoReason.setOnClickListener { v: View? ->
                    val body = JSONObject()
                    try {
                        body.put("user_id", User.userId)
                        body.put("task_id", task.id)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    val request = JsonArrayRequest(Request.Method.POST,
                            context!!.getString(R.string.declineTaskUrl),
                            body,
                            Response.Listener { response: JSONArray? -> },
                            Response.ErrorListener { error: VolleyError? -> })
                    val requestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(request)
                    dialog!!.cancel()
                    closeAll()
                    updateList(TASKS_IN_PROCESS)
                }
                penalty.text = "Со счета будет вычтен штраф " + task.penalty + " баллов"
                builder.setView(refusePenaltyView)
            }
            "editReport" -> {
                val editView = layoutInflater.inflate(R.layout.edit_report_dailog, null)
                editReportImage = editView.findViewById(R.id.editReportImage)
                editReportImage.setVisibility(View.GONE)
                val comment = editView.findViewById<TextView>(R.id.editReportTextBox)
                val chooseAttachType = editView.findViewById<Button>(R.id.editReportAttachImage)
                chooseAttachType.setOnClickListener { v: View? ->
                    val p = PopupMenu(this@TasksActivity, chooseAttachType)
                    p.menuInflater.inflate(R.menu.choose_image_type_popup, p.menu)
                    p.setOnMenuItemClickListener { item: MenuItem ->
                        if (item.title == "Сделать фотографию") {
                            //получение фото через камеру
                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            // Ensure that there's a camera activity to handle the intent
                            if (takePictureIntent.resolveActivity(packageManager) != null) {
                                // Create the File where the photo should go
                                var photoFile: File? = null
                                try {
                                    photoFile = createImageFile()
                                } catch (ignored: IOException) {
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    val photoURI = FileProvider.getUriForFile(this@TasksActivity,
                                            "ru.mpei.vmss.myapplication.fileprovider",
                                            photoFile)
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    startActivityForResult(takePictureIntent, TAKE_PHOTO)
                                }
                            }
                        } else {
                            //получение фото из галереи
                            val pickPhoto = Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(pickPhoto, CHOOSE_PHOTO) //one can be replaced with any action code
                        }
                        true
                    }
                    p.show()
                }
                val editReport = editView.findViewById<Button>(R.id.editReportButton)
                editReport.setOnClickListener { v: View? ->
                    val body = JSONObject()
                    try {
                        body.put("user_id", User.userId)
                        body.put("task_id", task.id)
                        body.put("comment", comment.text)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    if (editReportImage.getDrawable() == null) {
                        Toast.makeText(context, "no", Toast.LENGTH_SHORT).show()
                    }
                    val request = JsonArrayRequest(Request.Method.POST,
                            context!!.getString(R.string.editReportUrl),
                            body,
                            Response.Listener { response: JSONArray? -> },
                            Response.ErrorListener { error: VolleyError? -> })
                    val requestQueue = Volley.newRequestQueue(context)
                    requestQueue.add(request)
                    dialog!!.cancel()
                }
                builder.setView(editView)
            }
        }
        dialog = builder.create()
        dialog!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                editReportImage!!.visibility = View.VISIBLE
                editReportImage!!.setImageURI(Uri.parse(currentPhotoPath))
            }
            CHOOSE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = imageReturnedIntent!!.data
                editReportImage!!.visibility = View.VISIBLE
                editReportImage!!.setImageURI(selectedImage)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }

    private fun closeAll() {
        for (g in 0 until adapter!!.groupCount) {
            list!!.collapseGroup(g)
        }
    }

    internal inner class specialAdapter(context: Context?, private val elements: List<Task>, type: Int) : BaseExpandableListAdapter() {
        private val inflater: LayoutInflater
        private val type: Int
        override fun getGroupCount(): Int {
            return elements.size
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return 1
        }

        override fun getGroup(groupPosition: Int): Any {
            return elements[groupPosition]
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any? {
            return null
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return 0
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        @SuppressLint("SetTextI18n")
        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.tasks_element, parent, false)
            }
            val taskNameText = view.findViewById<TextView>(R.id.taskNameText)
            taskNameText.text = getTasksElement(groupPosition).taskName
            val taskPriceText = view.findViewById<TextView>(R.id.taskPriceText)
            taskPriceText.text = getTasksElement(groupPosition).price + " б."
            val taskLocationText = view.findViewById<TextView>(R.id.taskLocationText)
            taskLocationText.text = "Место: " + getTasksElement(groupPosition).location
            return view
        }

        private fun getTasksElement(position: Int): Task {
            return getGroup(position) as Task
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View, parent: ViewGroup): View {
            var view = convertView
            when (this.type) {
                TASKS_IN_PROCESS -> {
                    if (view == null) {
                        view = inflater.inflate(R.layout.taken_tasks_element_child, parent, false)
                    }
                    val takenDescription = view.findViewById<TextView>(R.id.takenDescriptionText)
                    takenDescription.text = getTasksElement(groupPosition).taskDescription
                    val takenBeginTime = view.findViewById<TextView>(R.id.takenBeginText)
                    takenBeginTime.text = "Начало: " + getTasksElement(groupPosition).startDate
                    val takenEndTime = view.findViewById<TextView>(R.id.takenEndText)
                    takenEndTime.text = "Конец: " + getTasksElement(groupPosition).endDate
                    val takenRefuseBefore = view.findViewById<TextView>(R.id.takenCommentText)
                    takenRefuseBefore.text = "От задания можно отказаться не позднее, чем: " + getTasksElement(groupPosition).refuse_info
                    val finishBtn = view.findViewById<Button>(R.id.takenFinishButton)
                    finishBtn.setOnClickListener { v: View? -> showDialog("finish", getTasksElement(groupPosition)) }
                    val declineBtn = view.findViewById<Button>(R.id.takenDeclineButton)
                    declineBtn.setOnClickListener { v: View? ->
                        var time: Date? = null
                        try {
                            time = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(getTasksElement(groupPosition).refuse_info)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                        assert(time != null)
                        if (0 >= time!!.time - Date().time + 30000) {
                            showDialog("refusePenalty", getTasksElement(groupPosition))
                        } else {
                            showDialog("refuseNormal", getTasksElement(groupPosition))
                        }
                    }
                }
                TASKS_IN_CHECK -> {
                    if (view == null) {
                        view = inflater.inflate(R.layout.checking_tasks_element_child, parent, false)
                    }
                    val checkingStatus = view.findViewById<TextView>(R.id.checkingStatusText)
                    checkingStatus.text = "Статус: " + getTasksElement(groupPosition).status
                    val changeTime = view.findViewById<TextView>(R.id.checkingChangeTimeText)
                    changeTime.text = "Отредактировать отчет можно до: " + getTasksElement(groupPosition).changeBefore
                    val editReportBtn = view.findViewById<Button>(R.id.checkingAgreeButton)
                    editReportBtn.setOnClickListener { v: View? -> showDialog("editReport", getTasksElement(groupPosition)) }
                }
                else -> {
                    if (view == null) {
                        view = inflater.inflate(R.layout.finished_tasks_element_child, parent, false)
                    }
                    val description = view.findViewById<TextView>(R.id.finishedDescriptionText)
                    description.text = getTasksElement(groupPosition).taskDescription
                }
            }
            return view
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return false
        }

        init {
            inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            this.type = type
        }
    }
}