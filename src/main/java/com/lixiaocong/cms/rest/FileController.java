/*
  BSD 3-Clause License

  Copyright (c) 2016, lixiaocong(lxccs@iCloud.com)
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name of the copyright holder nor the names of its
    contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.lixiaocong.cms.rest;

import com.lixiaocong.cms.service.ImageCodeService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@RestController
@RequestMapping("/file")
public class FileController {
    private final ImageCodeService codeService;
    private Log logger = LogFactory.getLog(getClass());

    private String fileServerRoot;
    private String fileServerUrl;

    @Autowired
    public FileController(ImageCodeService codeService, @Value("${file.server.root}") String fileServerRoot, @Value("${file.server.url}") String fileServerUrl) {
        this.codeService = codeService;
        this.fileServerRoot = fileServerRoot;
        if (!this.fileServerRoot.endsWith("/"))
            this.fileServerRoot += "/";
        this.fileServerUrl = fileServerUrl;
        if (!this.fileServerUrl.endsWith("/"))
            this.fileServerUrl += "/";
    }

    @RolesAllowed("ROLE_USER")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String post(MultipartFile imageFile) {
        try {
            File newFile = new File(fileServerRoot + "image/" + UUID.randomUUID() + imageFile.getOriginalFilename());
            imageFile.transferTo(newFile);
            return fileServerUrl + "image/" + newFile.getName();
        } catch (Exception e) {
            logger.error(e);
        }
        return fileServerUrl + "image/" + "error.jpg";
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/video", method = RequestMethod.GET)
    public Map<String, Object> video() {
        File folder = new File(fileServerRoot);
        if (!folder.exists()) return ResponseMsgFactory.createFailedResponse("目标文件夹不存在");
        Collection<File> files = FileUtils.listFiles(folder, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE);
        List<String> fileList = new LinkedList<>();
        for (File video : files) {
            if (video.isFile()) fileList.add(video.getName());
        }
        Map<String, Object> ret = ResponseMsgFactory.createSuccessResponse("videos", fileList);
        ret.put("serverUrl", fileServerUrl);
        return ret;
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/video", method = RequestMethod.DELETE)
    public Map<String, Object> delete(@RequestParam String fileName) {
        File file = new File(fileServerRoot + fileName);
        if (!file.exists()) return ResponseMsgFactory.createFailedResponse("文件不存在");
        else if (!file.isFile()) return ResponseMsgFactory.createFailedResponse("不是文件");
        else if (!file.delete()) return ResponseMsgFactory.createFailedResponse("删除不成功");
        return ResponseMsgFactory.createSuccessResponse();
    }

    @RequestMapping(value = "/imagecode")
    public void imagecode(HttpSession session, HttpServletResponse response) throws IOException {
        response.setContentType("image/png");
        OutputStream os = response.getOutputStream();
        ImageIO.write(codeService.getImage(session), "png", os);
    }
}
