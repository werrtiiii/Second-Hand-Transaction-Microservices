package com.secondhand.report.service;

import com.secondhand.common.AppException;
import com.secondhand.product.service.ProductService;
import com.secondhand.report.entity.Report;
import com.secondhand.report.entity.ReportReason;
import com.secondhand.report.entity.ReportStatus;
import com.secondhand.report.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportService {
    @org.springframework.beans.factory.annotation.Autowired private com.secondhand.micro.platform.Outbox outbox;

    private final ReportRepository reportRepo;
    private final ProductService productService;

    public ReportService(ReportRepository reportRepo, ProductService productService) {
        this.reportRepo = reportRepo;
        this.productService = productService;
    }

    @Transactional
    public Report submit(Long reporterId, Long productId, ReportReason reasonType, String description) {
        if (productService.getById(productId).getSellerId().equals(reporterId)) {
            throw new AppException("FORBIDDEN", "不能举报自己的商品", HttpStatus.FORBIDDEN);
        }
        Report r = new Report();
        r.setReporterId(reporterId);
        r.setProductId(productId);
        r.setReasonType(reasonType);
        r.setDescription(description);
        r.setStatus(ReportStatus.PENDING);
        r.setCreatedAt(LocalDateTime.now());
        Report saved=reportRepo.save(r);
        if(r.getStatus()!=ReportStatus.PENDING){
            var message=java.util.Map.of("id","report-"+r.getId(),"type","report_"+r.getStatus().name().toLowerCase(),"title","举报处理结果","content",r.getHandleNote()==null?r.getStatus().name():r.getHandleNote(),"relatedId",String.valueOf(r.getProductId()),"time",r.getHandledAt().toString());
            outbox.enqueue(r.getReporterId(),"system",message);
            if(r.getStatus()==ReportStatus.HANDLED){var sellerMessage=new java.util.HashMap<String,Object>(message);sellerMessage.put("type","report_product_handled");outbox.enqueue(productService.getById(r.getProductId()).getSellerId(),"system",sellerMessage);}
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Report> listAll(int page, int size, ReportStatus status) {
        if (status != null) {
            return reportRepo.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
        }
        return reportRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Transactional
    public Report handle(Long reportId, Long adminId, String note) {
        Report r = reportRepo.findById(reportId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "举报不存在", HttpStatus.NOT_FOUND));
        r.setStatus(ReportStatus.HANDLED);
        r.setHandledAt(LocalDateTime.now());
        r.setHandledBy(adminId);
        r.setHandleNote(note);
        Report saved=reportRepo.save(r);
        if(r.getStatus()!=ReportStatus.PENDING){
            var message=java.util.Map.of("id","report-"+r.getId(),"type","report_"+r.getStatus().name().toLowerCase(),"title","举报处理结果","content",r.getHandleNote()==null?r.getStatus().name():r.getHandleNote(),"relatedId",String.valueOf(r.getProductId()),"time",r.getHandledAt().toString());
            outbox.enqueue(r.getReporterId(),"system",message);
            if(r.getStatus()==ReportStatus.HANDLED){var sellerMessage=new java.util.HashMap<String,Object>(message);sellerMessage.put("type","report_product_handled");outbox.enqueue(productService.getById(r.getProductId()).getSellerId(),"system",sellerMessage);}
        }
        return saved;
    }

    @Transactional
    public Report dismiss(Long reportId, Long adminId, String note) {
        Report r = reportRepo.findById(reportId)
                .orElseThrow(() -> new AppException("NOT_FOUND", "举报不存在", HttpStatus.NOT_FOUND));
        r.setStatus(ReportStatus.DISMISSED);
        r.setHandledAt(LocalDateTime.now());
        r.setHandledBy(adminId);
        r.setHandleNote(note);
        Report saved=reportRepo.save(r);
        if(r.getStatus()!=ReportStatus.PENDING){
            var message=java.util.Map.of("id","report-"+r.getId(),"type","report_"+r.getStatus().name().toLowerCase(),"title","举报处理结果","content",r.getHandleNote()==null?r.getStatus().name():r.getHandleNote(),"relatedId",String.valueOf(r.getProductId()),"time",r.getHandledAt().toString());
            outbox.enqueue(r.getReporterId(),"system",message);
            if(r.getStatus()==ReportStatus.HANDLED){var sellerMessage=new java.util.HashMap<String,Object>(message);sellerMessage.put("type","report_product_handled");outbox.enqueue(productService.getById(r.getProductId()).getSellerId(),"system",sellerMessage);}
        }
        return saved;
    }
}
