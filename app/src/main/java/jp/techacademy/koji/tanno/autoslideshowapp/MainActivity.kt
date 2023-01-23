package jp.techacademy.koji.tanno.autoslideshowapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.techacademy.koji.tanno.autoslideshowapp.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PERMISSIONS_REQUEST_CODE = 100
    private var cursor: Cursor? = null

    private var mTimer: Timer? = null
    private var mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 起動時に1枚目を表示
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                initCursor()
                if (cursor!!.moveToFirst()) showImageOfCursor()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        // Android 5系以下の場合
        } else {
            initCursor()
            if (cursor!!.moveToFirst()) showImageOfCursor()
        }

        binding.forwardButton.setOnClickListener {
            if (cursor != null) {
                if (cursor!!.moveToNext()) {
                    showImageOfCursor()
                } else if (cursor!!.moveToFirst()) {
                    showImageOfCursor()
                }
            }
        }
        binding.returnButton.setOnClickListener {
            if (cursor != null) {
                if (cursor!!.moveToPrevious()) {
                    showImageOfCursor()
                } else if (cursor!!.moveToLast()) {
                    showImageOfCursor()
                }
            }
        }
        binding.playstopButton.setOnClickListener {

            if (cursor != null) {
                // 再生処理
                if (binding.playstopButton.text == "再生") {

                    binding.playstopButton.text = "停止"
                    binding.returnButton.isEnabled = false
                    binding.forwardButton.isEnabled = false

                    if (mTimer == null) {
                        mTimer = Timer()
                        mTimer!!.schedule(object : TimerTask() {
                            override fun run() {
                                // 進むボタンの処理を呼び出してコードを簡易化できないか？
                                if (cursor!!.moveToNext()) {
                                    mHandler.post {
                                        showImageOfCursor()
                                    }
                                } else if (cursor!!.moveToFirst()) {
                                    mHandler.post {
                                        showImageOfCursor()
                                    }
                                }
                            }
                        }, 2000, 2000)
                    }

                    // 停止処理
                } else {
                    if (mTimer != null) {
                        mTimer!!.cancel()
                        mTimer = null
                    }
                    binding.playstopButton.text = "再生"
                    binding.returnButton.isEnabled = true
                    binding.forwardButton.isEnabled = true
                }
            }
        }

    }

    // Cursor初期化
    private fun initCursor() {
        // 画像の情報を取得する
        val resolver = contentResolver
        this.cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )!!  // なぜnonnull指定がいるのか？なぜテキストの例だといらないのか？
    }

    // 画像を表示
    private fun showImageOfCursor() {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            if (this.cursor != null) {
                val fieldIndex = this.cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = this.cursor!!.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                binding.imageView.setImageURI(imageUri)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.cursor!!.close()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可された
                    initCursor()
                    if (cursor!!.moveToFirst()) showImageOfCursor()
                } else {
                    // 許可されなかった
                    binding.playstopButton.isEnabled = false
                    binding.returnButton.isEnabled = false
                    binding.forwardButton.isEnabled = false
                }
        }
    }


}