package jp.techacademy.keito.nagata.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*


class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                Log.d("QA_App", "answer = "  + answer.toString())
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                    Log.d("QA_App", "return" )
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)
        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras


        mQuestion = extras!!.get("question") as Question
        Log.d("QA_App", "mQuestion = " + mQuestion.toString())

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            Log.d("QA_App", "QustionDetailActivity onCreate fav クリックされた")

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
            .child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)


//        UID
        var uid = FirebaseAuth.getInstance().currentUser!!.uid

        favoriteImageView.setOnClickListener {
            Log.d("QA_App", "QustionDetailActivity onCreate favoriteImageView クリックされた")
            Log.d("QA_App", "QustionDetailActivity onCreate genre = " + mQuestion.genre.toString())

            Log.d("QA_App", "QustionDetailActivity onCreate uid = " + uid.toString())

            var mGenre = mQuestion.genre

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val genreRef = dataBaseReference.child(UsersPATH).child(mQuestion.uid).child(GanrePATH).child(mGenre.toString()).child(mQuestion.questionUid)

            val data = HashMap<String, String>()

            Log.d("QA_App",   "generef = "+ genreRef.toString())
            Log.d("QA_App",   "data = "+ data.toString())

            var ansPath = mQuestion.questionUid

            data["favorites"] =  ansPath

            genreRef.setValue(data)


        }

    }

    override fun onResume() {
        super.onResume()

        val user = FirebaseAuth.getInstance().currentUser
       Log.d("QA_App", "QuestionDetailActivity onResume user = " + user.toString())

        val imageView = findViewById<ImageView>(R.id.favoriteImageView)
        Log.d("QA_App", "QuestionDetailActivity imageView = " + imageView.toString())

        if (user == null) {
//            ログインされていない場合はお気に入りを非表示に

            Log.d("QA_App", "QuestionDetailActivity onResume if 内")
            //非表示にしたい時に以下をする

            imageView.visibility = View.INVISIBLE

        } else {

            imageView.visibility = View.VISIBLE
        }


    }
}