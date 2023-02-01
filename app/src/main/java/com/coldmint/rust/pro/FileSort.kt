import java.io.File
import java.util.*

class FileSort(a: MutableList<File>, manner: Int) {
    val name = 0
    val time = 1
    val size = 2
    val type = 3
    //内置的类型排序筛选器数据 可以不用 但是类型排序必须要进行筛选才能正常排序
   init {
        when (manner) {
            name -> sort_name(a)
            time -> sort_time(a)
            size -> sort_size(a)
            type -> sort_type(a)
        }
//        println(manner)
        //一个内置的排序筛选器 也可以在外部调用 FileSort.sort_front(List<File> a) 等静态的方法
    }

    companion object {
        //0表示 名称排序  1表示 时间排序  2表示 大小排序  3表示类型排序
        lateinit var type_list: Array<String>

        fun sort_front(a: MutableList<File>) {
            Collections.sort(a, kotlin.Comparator { o1, o2 ->
                if (o1!!.isDirectory && o2!!.isFile) return@Comparator -1
                if (o1!!.isFile && o2!!.isDirectory) return@Comparator 1
                else return@Comparator 0
            })
        }

        //文件夹在前面的排序算法 每次sort_name等排序方法前后或者都会调研次方法
        fun sort_name(a: MutableList<File>) {
            Collections.sort(a, Comparator { o1, o2 ->
                if (o1!!.isDirectory && o2!!.isFile)
                    return@Comparator -1
                if (o1.isFile && o2!!.isDirectory)
                    return@Comparator 1
                else o1.name.compareTo(o2!!.name)
                return@Comparator 0
            })
            sort_front(a)
        }

        fun sort_time(a: MutableList<File>) {
            Collections.sort(a, Comparator { f1, f2 ->
                val diff = f1!!.lastModified() - f2!!.lastModified()
                if (diff > 0) 1 else if (diff == 0L) 0 else -1
                //如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            })
            sort_front(a)
        }

        fun sort_size(a: MutableList<File>) {
            Collections.sort(a, kotlin.Comparator { o1, o2 ->
                val diff = o1!!.length() - o2!!.length()
                if (diff > 0) 1
                else if (diff == 0L) 0
                else -1
            })
            sort_front(a)
        }

        fun sort_type(a: MutableList<File>) {
            //文件类型排序 按照后缀优先级
            sort_front(a)
            //先进行文件夹在前面的排序算法
            Collections.sort(a, Comparator { o1, o2 ->
                if (o1!!.isFile) {
                    val name = o1.name
                    val suffix = name.substring(name.lastIndexOf("."))
                    //获取name文件名字的后缀
                    val length = length(type_list, suffix)
                    //查找后缀是不是存在于 type_list数据中 如果存在返回存在位置 如果不存在则返回<0的值
                    if (length >= 0) return@Comparator -1
                } else if (o2!!.isFile) {
                    val name = o2.name
                    val suffix = name.substring(name.lastIndexOf("."))
                    //获取name文件名字的后缀
                    val length = length(type_list, suffix)
                    //查找后缀是不是存在于 type_list数据中 如果存在返回存在位置 如果不存在则返回<0的值
                    if (length >= 0) return@Comparator 1
                }
                0
            })
        }
        private fun length(sz: Array<String>, a: String): Int {
            //引入外部数据sz 去进行筛选让后返回位置
            for (i in sz.indices) {
                if (sz[i] == a) {
                    return i
                }
            }
            return -1
        }
    }
}