package com.coldmint.rust.pro
import com.coldmint.rust.pro.viewmodel.FileManagerViewModel
import java.io.File
import java.util.Collections

class FileSort(a: MutableList<File?>,sortType: FileManagerViewModel.SortType) {
    //内置的类型排序筛选器数据 可以不用 但是类型排序必须要进行筛选才能正常排序
   init {
        when (sortType) {
            FileManagerViewModel.SortType.BY_NAME -> sort_name(a)
            FileManagerViewModel.SortType.BY_LAST_MODIFIED -> sort_time(a)
            FileManagerViewModel.SortType.BY_SIZE -> sort_size(a)
            FileManagerViewModel.SortType.BY_TYPE -> sort_type(a)
        }
        sort_front(a)
//        println(manner)
        //一个内置的排序筛选器 也可以在外部调用 com.coldmint.rust.pro.FileSort.sort_front(List<File> a) 等静态的方法
    }

    companion object {
        //0表示 名称排序  1表示 时间排序  2表示 大小排序  3表示类型排序
        private fun sort_front(a: MutableList<File?>) {
            Collections.sort(a, kotlin.Comparator { o1, o2 ->
                if (o1!!.isDirectory && o2!!.isFile) return@Comparator -1
                if (o1.isFile && o2!!.isDirectory) return@Comparator 1
                else return@Comparator 0
            })
        }

        //文件夹在前面的排序算法 每次sort_name等排序方法前后或者都会调研次方法
        fun sort_name(a: MutableList<File?>) {
            Collections.sort(a, Comparator { o1, o2 ->
                if (o1!!.isDirectory && o2!!.isFile)
                    return@Comparator -1
                if (o1.isFile && o2!!.isDirectory)
                    return@Comparator 1
                else o1.name.compareTo(o2!!.name)
                return@Comparator 0
            })
        }

        fun sort_time(a: MutableList<File?>) {
            a.sortWith { f1, f2 ->
                val diff = f1!!.lastModified() - f2!!.lastModified()
                if (diff > 0) 1 else if (diff == 0L) 0 else -1
                //如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }
        }

        fun sort_size(a: MutableList<File?>) {
            a.sortWith { o1, o2 ->
                val diff = o1!!.length() - o2!!.length()
                if (diff > 0) 1
                else if (diff == 0L) 0
                else -1
            }
        }

        fun sort_type(a: MutableList<File?>) {
            //文件类型排序 按照后缀优先级
            a.sortWith { o1, o2 ->
                val extension1 = o1?.extension?.lowercase()
                val extension2 = o2?.extension?.lowercase()
                // 定义文件类型的优先级顺序
                val typePriority = listOf("txt", "jpg", "png")
                val typeIndex1 = typePriority.indexOf(extension1)
                val typeIndex2 = typePriority.indexOf(extension2)
                typeIndex1.compareTo(typeIndex2)
            }
        }
    }
}