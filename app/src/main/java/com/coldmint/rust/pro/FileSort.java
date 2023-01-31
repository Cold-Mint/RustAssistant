import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileSort {
    final int name=0,time=1,size=2,type=3;
    public static String[] type_list;
    public FileSort(List<File> a,int manner){
        switch (manner){
            case name:
                sort_name(a);
                break;
            case time:
                sort_time(a);
                break;
            case size:
                sort_size(a);
                break;
            case type:
                sort_type(a);
                break;
            default:
        }
    }
    public static void sort_front(List<File> a){
        Collections.sort(a, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return 0;
            }
        });
    }

    public static void sort_name(List<File> a){
        Collections.sort(a,new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        sort_front(a);
    }
    public static void sort_time(List<File> a){
        Collections.sort(a,new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
                //如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }
        });
        sort_front(a);
    }

    public void sort_size(List<File> a){

        Collections.sort(a, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                long diff = o1.length() - o2.length();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
            }
        });
        sort_front(a);
    }
    public static void sort_type(List<File> a){
        //文件类型排序 按照后缀优先级
//        System.out.println(type_list[0]);
        Collections.sort(a, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isFile()) {
                  String name_a = o1.getName();
                  String o_z = name_a.substring(name_a.lastIndexOf("."));
//                  System.out.println(o_z);
                  //获取 name_a文件名字的后缀
                  int length=length(type_list,o_z);
                  System.out.println(length);
                  //查找后缀是不是存在于 type_list数据中 如果存在返回存在位置 如果不存在则返回<0的值
                  if (length>=0)
                      return -1;
                } else if (o2.isFile()) {
                    String name_a = o2.getName();
//                    System.out.println(name_a);
                    String o_z = name_a.substring(name_a.lastIndexOf("."));

                    //获取 name_a文件名字的后缀
                    int length=length(type_list,o_z);
                    if (length>=0)
                        return 1;
                }
                return 0;
            }
        });
    }
    private static int length(String a){
        for (int i=0;i<type_list.length;i++){
            if (type_list[i].equals(a)){
                return i;
            }
        }
        return -1;
    }
    private static int length(String[] sz,String a){
        for (int i=0;i<sz.length;i++){
            if (sz[i].equals(a)) {
                return i;
            }
        }
        return -1;
    }
}
