package com.babel88.paycal.controllers;

import com.babel88.paycal.api.DefaultPaymentModel;
import com.babel88.paycal.api.InvoiceDetails;
import com.babel88.paycal.api.ResultsViewer;
import com.babel88.paycal.api.controllers.BaseController;
import com.babel88.paycal.api.controllers.ReportControllers;
import com.babel88.paycal.api.logic.template.DefaultBaseLogicModel;
import com.babel88.paycal.config.factory.ControllerFactory;
import com.babel88.paycal.config.factory.GeneralFactory;
import com.babel88.paycal.config.factory.LogicFactory;
import com.babel88.paycal.config.factory.ModelViewFactory;
import com.babel88.paycal.logic.NullPaymentModelPassedException;
import com.babel88.paycal.models.PaymentModel;
import com.babel88.paycal.view.ResultsOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Created by edwin.njeru on 25/08/2017.
 */
public abstract class AbstractBaseController implements BaseController {

    //TODO register this in controller factory
    //TODO create base controller for updating PaymentModel
    //TODO >> Abstract methods to fetch custom data
    //TODO base logic model implementation
    //TODO review methods in this class and subclass with controllers

    private final Logger log = LoggerFactory.getLogger(AbstractBaseController.class);
    public ResultsViewer viewResults;
    private Boolean doAgain;
    private InvoiceDetails invoice;
    private ReportControllers reportsController;
    private DefaultPaymentModel defaultPaymentModel;
    private DefaultBaseLogicModel defaultBaseLogicModel;

    private BigDecimal invoiceAmount;

    protected AbstractBaseController() {

        log.debug("Creating the abstractBaseController...");

        log.debug("Fetching the reports controller from the controller factory");
        reportsController = ControllerFactory.getInstance().createReportController();
        log.debug("Fetching the view results from the model view factory");
        viewResults = ModelViewFactory.getInstance().createResultsViewer();
        log.debug("Fetching the invoice object from the general factory");
        invoice = GeneralFactory.getInstance().createInvoice();
        log.debug("Fetching the default payment model from the subclass");
        defaultPaymentModel = getDefaultPaymentModelInstance();
        log.debug("Fetching the default base logic model from the subclass");
        defaultBaseLogicModel = getDefaultBaseLogicModelInstance();

        log.debug("Checking if the defaultPaymentModel is null");
        baseModelsNullCheck(defaultPaymentModel);

        log.debug("Checking if the defaultBaseLogicModel is null");
        baseModelsNullCheck(defaultBaseLogicModel);
    }

    public BigDecimal getInvoiceAmount(){

        invoiceAmount = invoice.invoiceAmount();

        return invoiceAmount;
    }


    /**
     * This is the main method of this class
     */
    @Override
    public void invoke() {

        log.debug("Invoke method called..., Fetching the invoice amount from user...");


        BigDecimal amountBeforeTax = defaultBaseLogicModel.getAmountBeforeTax(invoiceAmount);
        //TODO update logic classes to accept amount before taxes

        ResultsOutput resultsOutput;

        do {
            log.debug("Starting the controller loop...");

            updateWithholdingVat(amountBeforeTax);

            updateTotalExpense(invoiceAmount);

            updateWithholdingTax(invoiceAmount);

            updatePrepayableAmount();

            updatePayableToVendor();

            log.debug("Creating the results output from the view results object, and displaying the \n" +
                    "data in a table string, using the defaultPaymentModel : {}.", defaultPaymentModel);
            resultsOutput = (ResultsOutput) viewResults.forPayment((PaymentModel) defaultPaymentModel);

            doAgain = invoice.doAgain();

            log.debug("User has chosen to repeat the calculation : {}.", doAgain);
        } while (doAgain);

        log.debug("Calling the reports controller to print the report, for the resultsOutput \n" +
                "object : {}.", resultsOutput);
        reportsController.printReport().forPayment(resultsOutput);
    }

    /**
     * updates the withholding vat in the payment model
     *
     * @param amountBeforeTax supplied at runtime
     */
    @Override
    public void updateWithholdingVat(BigDecimal amountBeforeTax) {

        log.debug("updateWithholdingVat( {}. ) method called...", amountBeforeTax);
        defaultPaymentModel.setWithHoldingVat(
                defaultBaseLogicModel.calculateWithholdingVat(amountBeforeTax)
        );
    }

    /**
     * updates the total expense in the payment model
     *
     * @param invoiceAmount supplied at runtime
     */
    @Override
    public void updateTotalExpense(BigDecimal invoiceAmount) {
        log.debug("updateTotalExpense( {}. ) method called...", invoiceAmount);
        defaultPaymentModel.setTotalExpense(
                defaultBaseLogicModel.calculateTotalExpense(invoiceAmount)
        );
    }

    /**
     * calculates the withholding tax in the payment model
     *
     * @param invoiceAmount supplied at runtime
     */
    @Override
    public void updateWithholdingTax(BigDecimal invoiceAmount) {
        log.debug("updateWithholdingTax( {}. ) method called...", invoiceAmount);
        defaultPaymentModel.setWithholdingTax(
                defaultBaseLogicModel.calculateWithholdingTax(invoiceAmount)
        );
    }

    /**
     * gets amount for prepayment and updates the payment model
     */
    @Override
    public void updatePrepayableAmount() {

        log.debug("updatePrepayableAmount() method called...");
        defaultPaymentModel.setToPrepay(
                defaultBaseLogicModel.calculateToPrepay(defaultPaymentModel)
        );
    }

    /**
     * gets amount calculataed for payee an updates the payment model
     */
    @Override
    public void updatePayableToVendor() {

        log.debug("updatePrepayableAmount() method called...");
        try {
            defaultPaymentModel.setToPayee(
                    defaultBaseLogicModel.calculateToPayee(defaultPaymentModel)
            );
        } catch (NullPaymentModelPassedException e) {
            log.debug("Whoa!! Did someone pass a null defaultPaymentModel object ? :");
            e.printStackTrace();
        }
    }

    /**
     * Gives the amount due for settlement as per invoice request
     *
     * @return
     */
    @Override
    public BigDecimal getInvoiceAmount() {

        log.debug("Running the getInvoiceAmount(0 method from the hopefully, \n" +
                "not null object,{}.", invoice.getClass());
        invoice = (invoice != null) ? invoice : GeneralFactory.getInstance().createInvoice();

        return invoice.invoiceAmount();
    }

    public abstract void setInvoiceAmount(BigDecimal invoiceAmount);

    private void baseModelsNullCheck(DefaultPaymentModel defaultPaymentModel) {

        log.debug("The baseModelsNullCheck( {}. ) has been called to check if any of the arguments \n" +
                        "is null. If so both methods are called from the factory to avoid null pointer exception \n" +
                        "with default behaviour. As a user you do not want this to happen at all",
                defaultPaymentModel.getClass());

        log.debug("Checking if the defaultPaymentModel is null");
        if (defaultPaymentModel != null) {

            // Do nothing
            log.debug("Good boy. The defaltPaymentModel : {}. is not null, proceeding with controller logic",
                    defaultPaymentModel.getClass());
        } else {

            log.debug("The developer has failed us, by calling a null defaultPaymentModel, calling \n" +
                    "the object from the factory");
            //TODO defaultPaymentModel = ModelFactory.getInstance().createDefaultPaymentModel();
        }
    }

    private void baseModelsNullCheck(DefaultBaseLogicModel defaultBaseLogicModel) {

        log.debug("The baseModelsNullCheck( {}. ) has been called to check if any of the arguments \n" +
                        "is null. If so both methods are called from the factory to avoid null pointer exception \n" +
                        "with default behaviour. As a user you do not want this to happen at all",
                defaultBaseLogicModel.getClass());

        log.debug("Checking if the defaultPaymentModel is null");
        if (defaultBaseLogicModel != null) {

            // Do nothing
            log.debug("Good boy. The defaltPaymentModel : {}. is not null, proceeding with controller logic",
                    defaultBaseLogicModel.getClass());
        } else {

            log.debug("The developer has failed us, by calling a null defaultPaymentModel, calling \n" +
                    "the object from the factory");
            //defaultBaseLogicModel = LogicFactory.getInstance().createBaseLogicModel();
        }
    }


}