package com.babel88.paycal.controllers.base;

import com.babel88.paycal.api.DefaultPaymentModel;
import com.babel88.paycal.api.controllers.DefaultControllers;
import com.babel88.paycal.api.logic.DefaultLogic;
import com.babel88.paycal.config.factory.LogicFactory;
import com.babel88.paycal.controllers.PaymentsControllerRunnerImpl;
import com.babel88.paycal.models.TTArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Thi controller replaces the deprecated Typical payments controller, inorder to
 * provide adherence to DefaultControllers interface which is also compatible to
 * the PrepaymentDelegate
 * <p>
 * Created by edwin.njeru on 30/08/2017.
 */
public class DefaultTypicalPaymentsController extends PaymentsControllerRunnerImpl implements DefaultControllers {

    private static final Logger log = LoggerFactory.getLogger(RentalPaymentsController.class);
    private static DefaultControllers instance = new DefaultTypicalPaymentsController();

    @Inject
    private DefaultLogic typicalPaymentsLogic;

    private DefaultTypicalPaymentsController() {
        super();
        log.debug("Creating a rental payments controller using constructor in PaymentsControllerRunner : {}",this);
    }

    @Override
    public void updateTotalExpense() {

        super.paymentModel.setTotalExpense(
                typicalPaymentsLogic.calculateTotalExpense(invoiceAmount)
        );
    }

    @Override
    public void updateToPayee() {

        super.paymentModel.setToPayee(
                typicalPaymentsLogic.calculateToPayee(invoiceAmount)
        );

    }

    @Override
    public void updateWithholdingTax() {

        super.paymentModel.setWithholdingTax(
                typicalPaymentsLogic.calculateWithholdingTax(invoiceAmount)
        );
    }

    @Override
    public void updateWithholdingVat() {

        super.paymentModel.setWithHoldingVat(
                typicalPaymentsLogic.calculateWithholdingVat(invoiceAmount)
        );

    }

    @Override
    public DefaultPaymentModel getPaymentModel() {

        return super.paymentModel;
    }

    @Override
    public TTArguments getTtArguments() {
        return null;
    }
}
