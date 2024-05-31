package xu.yuan.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import xu.yuan.properties.OOSproperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class AliOSSUtils {
    @Autowired//将OSS的配置注入进来，后面需要用到
    private OOSproperty ooSproperty;
 
    /**
     * 以下是一个实现上传文件到OSS的方法，并不唯一
     */
 
    public String upload(MultipartFile file) throws IOException {
        //获取阿里云OSS参数
        String endpoint = ooSproperty.getEndPoint();
        String accessKeyId = ooSproperty.getAccessKey();
        String accessKeySecret = ooSproperty.getSecretKey();
        String bucketName = ooSproperty.getBucketName();
 
        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();
 
        // 避免文件覆盖，需要使用UUID将文件重命名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名为空");
        }
        //判断文件是否有扩展名
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex == -1) {
            throw new IllegalArgumentException("文件名不包含扩展名");
        }
        // 如果hello.txt
        //  originalFilename.substring(dotIndex) ==> 截取到的是 .txt
        String fileName = UUID.randomUUID() + originalFilename.substring(dotIndex);
 
        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);
 
        // 文件访问路径，可自行更改逻辑，重点是拼接url
        String[] endpointParts = endpoint.split("\\.");
        if (endpointParts.length != 3) {
            throw new IllegalArgumentException("无效的 endpoint 格式");
        }
        //url拼接
        String url = "https://" + bucketName + "." + endpointParts[0] + "." + endpointParts[1] +  "." + endpointParts[2] + "/" + fileName;
        //文件访问路径，若使用的yml，并配置了正确的endpoint，可使用以下方式
        //String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
 
 
        // 关闭ossClient
        ossClient.shutdown();
 
        // 把上传到oss的路径返回
        return url;
    }
}