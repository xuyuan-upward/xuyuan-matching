package xu.yuan.one;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * @ClassName: yupao-backend01
 * @Description:    导入Excel，读取数据
 */
public class ImportExcel {
    /**
     * 读取数据
     */
    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        //Excel数据文件放在自己电脑上，能够找到的路径
        String fileName = "D:\\MachineLanguageLearning\\javaAll\\yupao-backend\\src\\main\\resources\\testExcel.xlsx";
//          readByListener(fileName);
        synchronousRead(fileName);

    }
    /**
     * 监听器读取
     * @param fileName
     */
    public static void readByListener(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuTableUserInfo> list = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuTableUserInfo : list) {
            System.out.println(xingQiuTableUserInfo);
        }

    }
}
