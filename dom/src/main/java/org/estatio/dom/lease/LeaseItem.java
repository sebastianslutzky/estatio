package org.estatio.dom.lease;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.collect.Ordering;

import org.estatio.dom.charge.Charge;
import org.estatio.dom.charge.Charges;
import org.estatio.dom.utils.CalenderUtils;
import org.estatio.dom.utils.Orderings;
import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

@PersistenceCapable
@javax.jdo.annotations.Version(strategy = VersionStrategy.VERSION_NUMBER, column = "VERSION")
public class LeaseItem extends AbstractDomainObject implements Comparable<LeaseItem> {

    @Hidden
    void dummyAction1(LeaseTermForIndexableRent x) {
    }

    @Hidden
    void dummyAction2(LeaseTermForTurnoverRent x) {
    }

    // {{ Lease (property)
    private Lease lease;

    @Hidden(where = Where.PARENTED_TABLES)
    @Title(sequence="1")
    @MemberOrder(sequence = "1")
    public Lease getLease() {
        return lease;
    }

    public void setLease(final Lease lease) {
        this.lease = lease;
    }

    // }}

    // {{ Sequence (property)
    private BigInteger sequence;

    @MemberOrder(sequence = "1")
    public BigInteger getSequence() {
        return sequence;
    }

    public void setSequence(final BigInteger sequence) {
        this.sequence = sequence;
    }

    // }}

    // {{ LeaseItemType (property)
    private LeaseItemType type;

    @Title(sequence="2", prepend=":")
    @MemberOrder(sequence = "2")
    public LeaseItemType getType() {
        return type;
    }

    public void setType(final LeaseItemType type) {
        this.type = type;
    }

    // }}

    // {{ StartDate (property)
    private LocalDate startDate;

    @Persistent
    @MemberOrder(sequence = "3")
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    // }}

    // {{ EndDate (property)
    private LocalDate endDate;

    @Persistent
    @MemberOrder(sequence = "4")
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    // }}

    // {{ TenancyStartDate (property)
    private LocalDate tenancyStartDate;

    @Persistent
    @Optional
    @MemberOrder(sequence = "5")
    @Hidden(where = Where.PARENTED_TABLES)
    public LocalDate getTenancyStartDate() {
        return tenancyStartDate;
    }

    public void setTenancyStartDate(final LocalDate tenancyStartDate) {
        this.tenancyStartDate = tenancyStartDate;
    }

    // }}

    // {{ TenancyEndDate (property)
    private LocalDate tenancyEndDate;

    @Persistent
    @Optional
    @MemberOrder(sequence = "6")
    @Hidden(where = Where.PARENTED_TABLES)
    public LocalDate getTenancyEndDate() {
        return tenancyEndDate;
    }

    public void setTenancyEndDate(final LocalDate tenancyEndDate) {
        this.tenancyEndDate = tenancyEndDate;
    }

    // }}

    // {{ NextDueDate (property)
    private LocalDate nextDueDate;

    @MemberOrder(sequence = "7")
    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(final LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    // }}

    // {{ InvoicingFrequency (property)
    private InvoicingFrequency invoicingFrequency;

    @MemberOrder(sequence = "12")
    public InvoicingFrequency getInvoicingFrequency() {
        return invoicingFrequency;
    }

    public void setInvoicingFrequency(final InvoicingFrequency invoicingFrequency) {
        this.invoicingFrequency = invoicingFrequency;
    }

    // }}

    // {{ PayymentMethod (property)
    private PaymentMethod paymentMethod;

    @MemberOrder(sequence = "13")
    public PaymentMethod getPayymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // }}

    // {{ Charge (property)
    private Charge charge;

    @MemberOrder(sequence = "14")
    public Charge getCharge() {
        return charge;
    }

    public void setCharge(final Charge charge) {
        this.charge = charge;
    }

    public List<Charge> choicesCharge() {
        return chargeService.allCharges();
    }

    // }}

    // {{ CurrentValue
    public BigDecimal getCurrentValue() {
        for (LeaseTerm term : getTerms()) {
            if (CalenderUtils.isBetween(LocalDate.now(), term.getStartDate(), term.getEndDate())) {
                return term.getValue();
            }
        }
        return null;
    }

    @Hidden
    public BigDecimal getValueForDate(LocalDate date) {
        for (LeaseTerm term : getTerms()) {
            if (CalenderUtils.isBetween(date, term.getStartDate(), term.getEndDate())) {
                return term.getValue();
            }
        }
        return null;
    }

    // }}

    // {{ Terms (Collection)
    private SortedSet<LeaseTerm> terms = new TreeSet<LeaseTerm>();

    @Render(Type.EAGERLY)
    @Persistent(mappedBy = "leaseItem")
    @MemberOrder(name = "Terms", sequence = "15")
    public SortedSet<LeaseTerm> getTerms() {
        return terms;
    }

    public void setTerms(final SortedSet<LeaseTerm> terms) {
        this.terms = terms;
    }

    public void addToTerms(final LeaseTerm leaseTerm) {
        // check for no-op
        if (leaseTerm == null || getTerms().contains(leaseTerm)) {
            return;
        }
        // associate new
        getTerms().add(leaseTerm);
        leaseTerm.setLeaseItem(this);
    }

    public void removeFromTerms(final LeaseTerm leaseTerm) {
        // check for no-op
        if (leaseTerm == null || !getTerms().contains(leaseTerm)) {
            return;
        }
        // dissociate existing
        getTerms().remove(leaseTerm);
        leaseTerm.setLeaseItem(null);
    }

    @Hidden
    public LeaseTerm findTerm(LocalDate startDate) {
        for (LeaseTerm term : getTerms()) {
            if (startDate.equals(term.getStartDate())) {
                return term;
            }
        }
        return null;
    }

    @Hidden
    public LeaseTerm findTermForSequence(BigInteger sequence) {
        for (LeaseTerm term : getTerms()) {
            if (sequence.equals(term.getSequence())) {
                return term;
            }
        }
        return null;
    }

    // FIXME: move into the 'terms' collection once enablement/disablement is working in the wicket viewer.
    @MemberOrder(/*name = "terms",*/ sequence = "11")
    public LeaseTerm createInitialTerm() {
        LeaseTerm term = leaseTerms.newLeaseTerm(this);
        return term;
    }

    public String disableCreateInitialTerm() {
        return getTerms().size() > 0 ? "Use either 'Verify' or 'Create Next Term' on last term" : null;
    }


    // }}

    // {{ Actions

    public LeaseItem verify() {
        for (LeaseTerm term : getTerms()) {
            term.verify();
        }
        return this;
    }

    // }}

    // {{ Injected Services

    private Charges chargeService;

    public void setChargeService(Charges charges) {
        this.chargeService = charges;
    }

    private LeaseTerms leaseTerms;
    
    public void setLeaseTerms(LeaseTerms leaseTerms) {
        this.leaseTerms = leaseTerms;
    }
    
    // }}

    // {{ Comparable

    @Override
    public int compareTo(LeaseItem o) {
        return ORDERING_BY_TYPE.compound(ORDERING_BY_START_DATE).compare(this, o);
    }

    public static Ordering<LeaseItem> ORDERING_BY_TYPE = new Ordering<LeaseItem>() {
        public int compare(LeaseItem p, LeaseItem q) {
            return LeaseItemType.ORDERING_NATURAL.compare(p.getType(), q.getType());
        }
    };

    public final static Ordering<LeaseItem> ORDERING_BY_START_DATE = new Ordering<LeaseItem>() {
        public int compare(LeaseItem p, LeaseItem q) {
            return Orderings.lOCAL_DATE_NATURAL_NULLS_FIRST.compare(p.getStartDate(), q.getStartDate());
        }
    };

    // }}

}
