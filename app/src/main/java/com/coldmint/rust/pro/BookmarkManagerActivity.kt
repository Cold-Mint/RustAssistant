package com.coldmint.rust.pro


import android.annotation.SuppressLint
import com.coldmint.rust.pro.tool.BookmarkManager
import android.widget.EditText
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent

import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.coldmint.rust.pro.adapters.BookmarkAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databean.Bookmark
import com.coldmint.rust.pro.databinding.ActivityBookmarkManagerBinding
import com.coldmint.rust.pro.databinding.EditBookmarkBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import java.io.File
import java.util.ArrayList

class BookmarkManagerActivity : BaseActivity<ActivityBookmarkManagerBinding>() {
    private lateinit var bookmarkManager: BookmarkManager
    lateinit var bookmarkAdapter: BookmarkAdapter
    private lateinit var pathView: EditText
    private lateinit var nameView: EditText

    /**
     * 升级视图
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateView(bookmarks: ArrayList<Bookmark>?) {
        if (bookmarks == null || bookmarks.size <= 0) {
            viewBinding.progressBar.isVisible = false
            viewBinding.fileError.setText(R.string.no_bookmark)
        } else {
            val list = bookmarkManager.list()
            if (list != null && list.isNotEmpty()) {
                bookmarkAdapter =
                    BookmarkAdapter(this@BookmarkManagerActivity, list)
                bookmarkAdapter.setItemEvent { i, attachFileItemBinding, viewHolder, bookmark ->
                    attachFileItemBinding.root.setOnLongClickListener {
                        bookmarkAdapter.showDeleteItemDialog(
                            bookmark.name,
                            viewHolder.adapterPosition, onClickPositiveButton = { i:Int, b ->
                                bookmarkManager.removeBookmark(bookmark)
                                if (list.isEmpty()) {
                                    showNoBookmarkToView()
                                }
                                false
                            }
                        )
                        return@setOnLongClickListener false
                    }
                    attachFileItemBinding.root.setOnClickListener {
                        showEditView(getString(R.string.edit), bookmark.name, bookmark.path)
                    }
                }
                viewBinding.bookmarkList.adapter = bookmarkAdapter
                viewBinding.progressBar.isVisible = false
                viewBinding.fileError.isVisible = false
                viewBinding.bookmarkList.isVisible = true
            } else {
                showNoBookmarkToView()
            }
        }
    }

    /**
     * 显示没有书签到视图上
     */
    fun showNoBookmarkToView() {
        viewBinding.bookmarkList.isVisible = false
        viewBinding.fileError.isVisible = true
        viewBinding.progressBar.isVisible = false
        viewBinding.fileError.setText(R.string.no_bookmark)
    }

    /**
     * 展示编辑视图
     *
     * @param title 标题
     * @param name  名称
     * @param path  路径
     */
    fun showEditView(title: String, name: String?, path: String?) {
        val editBookmarkBinding = EditBookmarkBinding.inflate(layoutInflater)
        nameView = editBookmarkBinding.nameView
        pathView = editBookmarkBinding.pathEdit

        if (path != null) {
            editBookmarkBinding.pathEdit.setText(path)
        }
        if (name != null) {
            editBookmarkBinding.nameView.setText(name)
        }

        editBookmarkBinding.button.setOnClickListener {
            val bundle = Bundle()
            val intent =
                Intent(this@BookmarkManagerActivity, FileManagerActivity::class.java)
            bundle.putString("type", "selectDirectents")
            val thisPath = pathView.text.toString()
            if (thisPath.isNotEmpty()) {
                bundle.putString("path", thisPath)
            }
            //bundle.putString("rootpath", mRootPath);
            intent.putExtra("data", bundle)
            startActivityForResult(intent, 1)
        }

        var dialog: AlertDialog? = null
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            .setView(editBookmarkBinding.root)
            .setTitle(title)
            .setPositiveButton(R.string.dialog_ok)
            { i, i2 ->
                val newPath = pathView.text.toString()
                val newName = nameView.text.toString()
                if (newName.isEmpty()) {
                    setErrorAndInput(nameView, getString(R.string.enter_bookmark_name))
                    return@setPositiveButton
                }
                if (newPath.isEmpty()) {
                    setErrorAndInput(pathView, getString(R.string.enter_file_path))
                    return@setPositiveButton
                }
                val file = File(newPath)
                if (!file.exists()) {
                    setErrorAndInput(pathView, getString(R.string.bookmark_jump_failed))
                    return@setPositiveButton
                }
                if (name == null) {
                    val addBookmark = bookmarkManager.addBookmark(newPath, newName)
                    if (addBookmark) {
                        updateView(bookmarkManager.list())

                    } else {
                        setErrorAndInput(
                            pathView,
                            getString(R.string.bookmark_already_exists)
                        )
                    }
                } else {
                    if (name == newName && path == newPath) {
                        dialog?.dismiss()
                    } else {
                        if (path != null) {
                            val oldBookmark = Bookmark(path, name)
                            val newBookmark = Bookmark(newPath, newName)
                            val addBookmark =
                                bookmarkManager.replaceBookmark(oldBookmark, newBookmark)
                            if (addBookmark) {
                                updateView(bookmarkManager.list())
                                dialog?.dismiss()
                            } else {
                                setErrorAndInput(
                                    pathView,
                                    getString(R.string.bookmark_already_exists)
                                )
                            }
                        }
                    }
                }
            }
            .setNegativeButton(R.string.dialog_close) { i, i2 ->
                dialog?.dismiss()
            }
            .setCancelable(false)
        dialog = materialAlertDialogBuilder.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK) {
                val path = data!!.getStringExtra("Directents")
                pathView.setText(path)
                val oldName = nameView.text.toString()
                if (oldName.isEmpty()) {
                    val endIndex = path!!.lastIndexOf("/")
                    if (endIndex > -1) {
                        val name = path.substring(endIndex + 1, path.length)
                        nameView.setText(name)
                    } else {
                        nameView.setText(path)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                bookmarkManager.save()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            bookmarkManager.save()
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun getViewBindingObject(): ActivityBookmarkManagerBinding {
        return ActivityBookmarkManagerBinding.inflate(
            layoutInflater
        )
    }

    override fun whenCreateActivity(savedInstanceState: Bundle?, canUseView: Boolean) {
        if (canUseView) {
            setReturnButton()
            setTitle(R.string.bookmark_manager)
            bookmarkManager = BookmarkManager(this@BookmarkManagerActivity)
            bookmarkManager.load()
            viewBinding.bookmarkList.layoutManager =
                LinearLayoutManager(this@BookmarkManagerActivity)
            val bookmarks = bookmarkManager.list()
            updateView(bookmarks)
            viewBinding.fab.setOnClickListener {
                showEditView(
                    getString(R.string.create_bookmark),
                    null,
                    null
                )
            }
        }
    }
}