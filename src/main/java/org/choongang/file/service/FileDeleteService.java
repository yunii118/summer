package org.choongang.file.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.UnAuthorizedException;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.entities.QFileInfo;
import org.choongang.file.repositories.FileInfoRepository;
import org.choongang.member.MemberUtil;
import org.choongang.member.entities.AbstractMember;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileDeleteService {

    private final FileInfoService infoService;
    private final MemberUtil memberUtil;
    private final FileInfoRepository repository;

    public void delete(Long seq) {
        FileInfo data = infoService.get(seq);

        // 파일 삭제 권한 체크
        AbstractMember member = memberUtil.getMember();
        String createBy = data.getCreatedBy();
        if (StringUtils.hasText(createBy) && (!memberUtil.isLogin() || ///비회원일때
                (!memberUtil.isAdmin() && StringUtils.hasText(createBy) // 로그인한 회원이 작성했을때
                        && !createBy.equals(member.getUserId())))) {
            throw new UnAuthorizedException(Utils.getMessage("Not.yours.file", "errors"));
        }

        // 파일 삭제
        File file = new File(data.getFilePath());
        if (file.exists()) file.delete();

        List<String> thumbsPath = data.getThumbsPath();
        if(thumbsPath != null) {
            for (String path : thumbsPath) {
                File thumbFile = new File(path);
                if (thumbFile.exists()) thumbFile.delete();
            }
        }

        repository.delete(data);
        repository.flush();
    }

    public void delete(String gid, String location) {
        QFileInfo fileInfo = QFileInfo.fileInfo;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(fileInfo.gid.eq(gid));

        if (StringUtils.hasText(location)) {
            builder.and(fileInfo.location.eq(location));
        }

        List<FileInfo> items = (List<FileInfo>)repository.findAll(builder);

        items.forEach(i -> delete(i.getSeq()));
    }

    public void delete(String gid) {
        delete(gid, null);
    }
}
