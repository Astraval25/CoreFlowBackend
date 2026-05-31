package com.astraval.coreflow.main_modules.orderdetails.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.orderdetails.OrderDetails;
import com.astraval.coreflow.main_modules.orderdetails.OrderStatus;
import com.astraval.coreflow.main_modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.main_modules.notification.NotificationService;

@Service
public class PaymentDueReminderService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(
            cron = "#{@cronJobConfig.cron('paymentDueReminder')}",
            zone = "#{@cronJobConfig.zone('paymentDueReminder')}")
    @Transactional(readOnly = true)
    public void sendDuePaymentReminders() {
        LocalDate dueDate = LocalDate.now();
        LocalDateTime from = dueDate.atStartOfDay();
        LocalDateTime to = dueDate.plusDays(1).atStartOfDay();
        List<OrderDetails> dueOrders = orderDetailsRepository.findUnpaidOrdersByPaymentDueDateRange(
                from,
                to,
                OrderStatus.getOrderCancelled());

        for (OrderDetails order : dueOrders) {
            sendReminderForOrder(order, dueDate);
        }
    }

    private void sendReminderForOrder(OrderDetails order, LocalDate dueDate) {
        if (order == null || order.getOrderId() == null) {
            return;
        }

        Long sellerCompanyId = order.getCustomers() != null
                && order.getCustomers().getCompany() != null
                        ? order.getCustomers().getCompany().getCompanyId()
                        : null;
        Long buyerCompanyId = order.getVendors() != null
                && order.getVendors().getCompany() != null
                        ? order.getVendors().getCompany().getCompanyId()
                        : null;

        if (sellerCompanyId == null && buyerCompanyId == null) {
            return;
        }

        double total = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        double paid = order.getPaidAmount() != null ? order.getPaidAmount() : 0.0;
        double dueAmount = Math.max(total - paid, 0.0);
        if (dueAmount <= 0.0) {
            return;
        }

        String orderNumber = order.getOrderNumber() != null ? order.getOrderNumber() : ("#" + order.getOrderId());
        String dueDateText = dueDate.format(DATE_FMT);
        String amountText = String.format(Locale.ENGLISH, "%.2f", dueAmount);
        String message = "Payment reminder: Order " + orderNumber
                + " is due on " + dueDateText
                + ". Due amount: " + amountText + ".";

        if (buyerCompanyId != null && sellerCompanyId != null) {
            // Notify buyer side (vendor side)
            notificationService.createCompanyNotification(
                    sellerCompanyId,
                    buyerCompanyId,
                    "Payment Due Reminder",
                    message,
                    "PAYMENT_DUE_REMINDER",
                    "View Orders",
                    "/companies/" + buyerCompanyId + "/purchase/orders",
                    null,
                    "VENDOR",
                    order.getVendors() != null ? order.getVendors().getVendorId() : null);

            // Notify seller side (customer side)
            notificationService.createCompanyNotification(
                    buyerCompanyId,
                    sellerCompanyId,
                    "Payment Due Reminder",
                    message,
                    "PAYMENT_DUE_REMINDER",
                    "View Orders",
                    "/companies/" + sellerCompanyId + "/sales/orders",
                    null,
                    "CUSTOMER",
                    order.getCustomers() != null ? order.getCustomers().getCustomerId() : null);
        }
    }
}
