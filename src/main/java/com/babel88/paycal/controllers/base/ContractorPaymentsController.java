package com.babel88.paycal.controllers.base;

import com.babel88.paycal.api.DefaultPaymentModel;
import com.babel88.paycal.api.InvoiceDetails;
import com.babel88.paycal.api.ResultsViewer;
import com.babel88.paycal.api.controllers.DefaultControllers;
import com.babel88.paycal.api.controllers.PaymentsControllerRunner;
import com.babel88.paycal.api.controllers.PrepaymentController;
import com.babel88.paycal.api.controllers.ReportControllers;
import com.babel88.paycal.api.logic.DefaultLogic;
import com.babel88.paycal.api.logic.PrepaymentService;
import com.babel88.paycal.config.factory.*;
import com.babel88.paycal.controllers.prepayments.PrepaymentControllerImpl;
import com.babel88.paycal.controllers.delegate.PrepaymentsDelegate;
import com.babel88.paycal.models.PaymentModel;
import com.babel88.paycal.models.TTArguments;
import com.babel88.paycal.view.ResultsOutput;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@SuppressWarnings("ALL")
public class ContractorPaymentsController implements DefaultControllers, PaymentsControllerRunner {

    private static DefaultControllers instance = new ContractorPaymentsController();
    private final PrepaymentsDelegate prepaymentsDelegate = new PrepaymentsDelegate(this);
    private DefaultPaymentModel paymentModel;
    private InvoiceDetails invoice;
    private BigDecimal invoiceAmount;
    private DefaultLogic contractorLogic;
    private PrepaymentController prepaymentController;
    private ResultsViewer viewResults;
    private ReportControllers reportsController;
    private Boolean doAgain;

    public ContractorPaymentsController() {
        invoice = GeneralFactory.createInvoice();
        this.paymentModel = ModelFactory.createPaymentModel();
        contractorLogic = LogicFactory.getInstance().createContractorLogic();
        prepaymentController = ControllerFactory.getPrepaymentController();
        viewResults = ModelViewFactory.createResultsViewer();

        reportsController = ControllerFactory.getReportController();
    }

    public static DefaultControllers getInstance() {

        return instance;
    }

    @Override
    public void runCalculation() {

        ResultsOutput resultsOutput;

        do {
            invoiceAmount = invoice.invoiceAmount();
            updateTotalExpense();
            updateToPayee();
            updateWithholdingTax();
            updateWithholdingVat();
            prepaymentsDelegate.updateToPrepay();
            resultsOutput = (ResultsOutput) viewResults.forPayment((PaymentModel) paymentModel);
            // Results submitted for paymentModelView

            doAgain = invoice.doAgain();
        } while (doAgain);

        reportsController.printReport().forPayment(resultsOutput);
    }

    @Override
    public void updateTotalExpense() {

        paymentModel.setTotalExpense(
                contractorLogic.calculateTotalExpense(invoiceAmount)
        );
    }

    @Override
    public void updateToPayee() {

        paymentModel.setToPayee(
                contractorLogic.calculateToPayee(invoiceAmount)
        );
    }

    @Override
    public void updateWithholdingTax() {

        paymentModel.setWithholdingTax(
                contractorLogic.calculateWithholdingTax(invoiceAmount)
        );
    }

    @Override
    public void updateWithholdingVat() {

        paymentModel.setWithHoldingVat(
                contractorLogic.calculateWithholdingVat(invoiceAmount)
        );
    }

    @NotNull
    @Override
    public void updateToPrepay() {

        BigDecimal totalExpense = paymentModel.getTotalExpense();

        prepaymentController.setExpenseAmount(totalExpense);

        paymentModel.setToPrepay(
                ((PrepaymentService) prepaymentController::getPrepayment).prepay(totalExpense)
        );
    }

    @Override
    public DefaultPaymentModel getPaymentModel() {
        return paymentModel;
    }

    public DefaultControllers setPaymentModel(DefaultPaymentModel paymentModel) {
        this.paymentModel = paymentModel;
        return this;
    }

    public DefaultControllers setInvoice(InvoiceDetails invoice) {
        this.invoice = invoice;
        return this;
    }

    public DefaultControllers setInvoiceAmount(BigDecimal invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
        return this;
    }

    public DefaultControllers setContractorLogic(DefaultLogic defaultLogic) {
        this.contractorLogic = defaultLogic;
        return this;
    }

    public DefaultControllers setPrepaymentController(PrepaymentControllerImpl prepaymentController) {
        this.prepaymentController = prepaymentController;
        return this;
    }

    public DefaultControllers setViewResults(ResultsViewer viewResults) {
        this.viewResults = viewResults;
        return this;
    }

    public DefaultControllers setReportsController(ReportControllers reportsController) {
        this.reportsController = reportsController;
        return this;
    }

    @Override
    public com.babel88.paycal.api.controllers.PrepaymentController getPrepaymentController() {

        return prepaymentController;
    }

    @Override
    public TTArguments getTtArguments() {
        return null;
    }
}
