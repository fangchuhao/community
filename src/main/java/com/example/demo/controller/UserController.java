package com.example.demo.controller;

import com.example.demo.annotation.LoginRequest;
import com.example.demo.constant.CommunityConstant;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.Page;
import com.example.demo.entity.User;
import com.example.demo.service.DiscussPostService;
import com.example.demo.service.UserService;
import com.example.demo.util.CommonUtil;
import com.example.demo.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;
    @Value("${community.path.domain}")
    private String path;
    @Value("${community.path.upload}")
    private String upload;
    @Value("${community.headerUrl.type}")
    private String headerType;
    @Autowired
    private HostHolder hostHolder;

    // TODO 这里要注入七牛云的 accessKey secretKey bucket.header.httpUrl bucket.header.name
    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.httpUrl}")
    private String headerBucketUrl;


    private static final Logger log= LoggerFactory.getLogger(UserController.class);
    /**
     * 设置个人信息
     * @return
     */
    @LoginRequest
    @RequestMapping(value = "setUserInfo",method = RequestMethod.GET)
    public String setUserInfo(Model model) {
        User user = hostHolder.getUser();
        model.addAttribute("user",user);
        // TODO 如果有七牛云的话启用以下代码
//        // 上传文件名称
//        String fileName = CommonUtil.generateString();
//        // 设置响应信息
//        StringMap policy = new StringMap();
//        policy.put("returnBody", CommonUtil.getJSONString(0));
//        // 生成上传到七牛云的凭证
//        Auth auth=Auth.create(accessKey,secretKey);
//        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);
//        model.addAttribute("uploadToken",uploadToken);
//        model.addAttribute("fileName",fileName);
        return "site/setting.html";
    }

    // TODO 如果七牛云成功响应，调用该方法可以更新头像的路径
    @RequestMapping(value = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String getHeaderUrl(String fileName) {
        if(StringUtils.isBlank(fileName)) {
            return CommonUtil.getJSONString(1,"文件名不能为空！");
        }
        String url = headerBucketUrl+"/"+fileName;
        userService.setheaderUrl(hostHolder.getUser().getId(),url);

        return CommonUtil.getJSONString(0);
    }

    /**
     * 修改头像地址
     * @return
     */
    // 因为要上传到七牛云服务器，所以上传头像的该方法被废弃
    // TODO 需要废弃该方法
    @LoginRequest
    @RequestMapping(value = "setheaderUrl",method = RequestMethod.POST)
    public String setUserInfo(MultipartFile headerUrl,Model model) {
        if(headerUrl==null) {
            model.addAttribute("headerUrlMsg","请选择头像图片！");
            return "site/setting.html";
        }
        String originalFilename = headerUrl.getOriginalFilename();
        String type=originalFilename.substring(originalFilename.lastIndexOf(".")+1);
        if(!headerType.contains(type)) {
            model.addAttribute("headerUrlMsg","头像格式错误（应为.png/.jpg/.jpeg/.gif）！");
            return "site/setting.html";
        }
        long size=headerUrl.getSize();
        if(size>1024*1024) {
            model.addAttribute("headerUrlMsg","图片大小应小于1M！");
            return "site/setting.html";
        }
        File file=new File(upload);
        if(!file.exists()) {
            file.mkdirs();
        }
        String headerName=CommonUtil.generateString().substring(0,16);
        try {
            File storePath = new File(file + "/" + headerName + "." + type);
            headerUrl.transferTo(storePath);
            userService.setheaderUrl(hostHolder.getUser().getId(),path+"/user/header/"+headerName + "." + type);
            model.addAttribute("headerUrl", storePath);
        }catch (Exception e) {
            model.addAttribute("headerUrlMsg","头像上传失败！");
            return "site/setting.html";
        }
        model.addAttribute("headerUrlMsg","头像上传成功！");
        return "site/setting.html";
    }
    // TODO 需要废弃该方法
    @RequestMapping(value = "/header/{filename}",method = RequestMethod.GET)
    public void getPhoto(@PathVariable("filename")String filename,HttpServletResponse response) {
        if(StringUtils.isEmpty(filename)) {
            throw new IllegalArgumentException("头像地址错误！");
        }
        File storePath = new File(upload + '/' +filename);
        String suffix=filename.substring(filename.lastIndexOf(".")+1);

        response.setContentType("image/"+suffix);
        try(ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream fis=new FileInputStream(storePath);) {
            int len=0;
            byte[] b=new byte[1024];
            while((len=fis.read(b))>0) {
                outputStream.write(b,0,len);
            }
            log.info("头像查询成功！");
        }catch (Exception e) {
            log.warn("头像查询失败！");
        }
    }

    @RequestMapping("/setPassword")
    public String setPassword(
            @RequestParam(value = "oldPassword",required = true) String oldPassword,
            @RequestParam(value = "newPassword",required = true) String newPassword,
            Model model) {
        if(StringUtils.isEmpty(oldPassword)) {
            model.addAttribute("oldPasswordMsg","原始密码不能为空！");
            return "site/setting";
        }
        if(StringUtils.isEmpty(newPassword)) {
            model.addAttribute("newPasswordMsg","新密码密码不能为空！");
            return "site/setting";
        }
        User user = hostHolder.getUser();
        String salt=user.getSalt();
        String encryptOldPassword = CommonUtil.md5Encrypt(oldPassword+salt);

        if(!user.getPassword().equals(encryptOldPassword)) {
            model.addAttribute("oldPasswordMsg","原始密码错误！");
            return "site/setting";
        }
        if(oldPassword.equals(newPassword)) {
            model.addAttribute("newPasswordMsg","新密码不能和原密码相同！");
            return "site/setting";
        }
        try {
            int status=userService.setPassword(user.getId(),CommonUtil.md5Encrypt(newPassword+salt));
            if(status>0) {
                return "redirect:/logout";
            }
        }catch (Exception e) {
            log.info("修改密码失败！"+e.getMessage());
        }
        return "site/setting";
    }
}
