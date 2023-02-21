package com.coldmint.rust.pro


import android.annotation.SuppressLint
import com.coldmint.rust.pro.tool.BookmarkManager
import android.widget.EditText
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.coldmint.dialog.CoreDialog
import com.coldmint.rust.pro.adapters.BookmarkAdapter
import com.coldmint.rust.pro.base.BaseActivity
import com.coldmint.rust.pro.databean.Bookmark
import com.coldmint.rust.pro.databinding.ActivityBookmarkManagerBinding
import com.coldmint.rust.pro.databinding.EditBookmarkBinding
import com.coldmint.rust.pro.dialog.BookmarkDialog
import com.coldmint.rust.pro.ui.StableLinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import java.io.File
import java.util.ArrayList

class BookmarkManagerActivity : BaseActivity<ActivityBookmarkManagerBinding>() {
    private lateinit var bookmarkManager: BookmarkManager
    lateinit var bookmarkAdapter: BookmarkAdapter
    lateinit var bookmarkDialog :BookmarkDialog

    /**
     * 重新构建对话框（实例化一个对话框，并使成员变量指向他）
     */
    fun recreateBookmarkDialog(){
        bookmarkDialog = BookmarkDialog(this)
        bookmarkDialog.setButtonAction {
            val bundle = Bundle()
            val intent =
                Intent(this, FileManagerActivity::class.java)
            bundle.putString("type", "selectDirectents")
            val thisPath = bookmarkDialog.getPath()
            if (thisPath.isNotEmpty()) {
                bundle.putString("path", thisPath)
            }
            intent.putExtra("data", bundle)
            startActivityForResult(intent, 1)
        }
    }

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
                            viewHolder.absoluteAdapterPosition,
                            onClickPositiveButton = { i: Int, b ->
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
        recreateBookmarkDialog()
        if (path != null) {
            bookmarkDialog.setPathViewText(path)
        }
        if (name != null) {
            bookmarkDialog.setNameViewText(name)
        }
        bookmarkDialog.setTitle(title).setAutoDismiss(false).setCancelable(false)
        bookmarkDialog.setPositiveButton(R.string.dialog_ok) { editBookmarkBinding ->
            val newPath = editBookmarkBinding.pathEdit.text.toString()
            val newName = editBookmarkBinding.nameView.text.toString()
            if (newName.isEmpty()) {
                setErrorAndInput(
                    editBookmarkBinding.nameView,
                    getString(R.string.enter_bookmark_name), editBookmarkBinding.nameInputLayout
                )
                return@setPositiveButton
            }
            if (newPath.isEmpty()) {
                setErrorAndInput(
                    editBookmarkBinding.pathEdit,
                    getString(R.string.enter_file_path),
                    editBookmarkBinding.pathInputLayout
                )
                return@setPositiveButton
            }
            val file = File(newPath)
            if (!file.exists()) {
                setErrorAndInput(
                    editBookmarkBinding.pathEdit,
                    getString(R.string.bookmark_jump_failed),
                    editBookmarkBinding.pathInputLayout
                )
                return@setPositiveButton
            }
            if (name == null) {
                val addBookmark = bookmarkManager.addBookmark(newPath, newName)
                if (addBookmark) {
                    updateView(bookmarkManager.list())

                } else {
                    setErrorAndInput(
                        editBookmarkBinding.pathEdit,
                        getString(R.string.bookmark_already_exists),
                        editBookmarkBinding.pathInputLayout
                    )
                }
            } else {
                if (name == newName && path == newPath) {
                    bookmarkDialog.dismiss()
                } else {
                    if (path != null) {
                        val oldBookmark = Bookmark(path, name)
                        val newBookmark = Bookmark(newPath, newName)
                        val addBookmark =
                            bookmarkManager.replaceBookmark(oldBookmark, newBookmark)
                        if (addBookmark) {
                            updateView(bookmarkManager.list())
                            bookmarkDialog.dismiss()
                        } else {
                            setErrorAndInput(
                                editBookmarkBinding.pathEdit,
                                getString(R.string.bookmark_already_exists),
                                editBookmarkBinding.pathInputLayout
                            )
                        }
                    }
                }
            }
        }
        bookmarkDialog.setNegativeButton(R.string.dialog_close) {
            bookmarkDialog.dismiss()
        }
        bookmarkDialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK) {
                val path = data!!.getStringExtra("Directents")
                bookmarkDialog.setPathViewText(path)
                val oldName = bookmarkDialog.getName()
                if (oldName.isEmpty()) {
                    val endIndex = path!!.lastIndexOf("/")
                    if (endIndex > -1) {
                        val name = path.substring(endIndex + 1, path.length)
                        bookmarkDialog.setNameViewText(name)
                    } else {
                        bookmarkDialog.setNameViewText(path)
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

    override fun getViewBindingObject(layoutInflater: LayoutInflater): ActivityBookmarkManagerBinding {
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
                StableLinearLayoutManager(this@BookmarkManagerActivity)
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