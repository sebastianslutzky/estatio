package org.estatio.dom.agreement;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberGroups;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

import org.estatio.dom.EstatioTransactionalObject;
import org.estatio.dom.Lockable;
import org.estatio.dom.Status;
import org.estatio.dom.WithIntervalMutable;
import org.estatio.dom.WithNameGetter;
import org.estatio.dom.WithReferenceComparable;
import org.estatio.dom.party.Party;
import org.estatio.dom.utils.ValueUtils;
import org.estatio.dom.valuetypes.LocalDateInterval;

@javax.jdo.annotations.PersistenceCapable
@javax.jdo.annotations.Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME)
@javax.jdo.annotations.Version(strategy = VersionStrategy.VERSION_NUMBER, column = "VERSION")
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
            name = "findByReference", language = "JDOQL", 
            value = "SELECT FROM org.estatio.dom.agreement.Agreement WHERE reference.matches(:reference)"),
        @javax.jdo.annotations.Query(
            name = "findByAgreementTypeAndRoleTypeAndParty", language = "JDOQL", 
            value = "SELECT " + 
                    "FROM org.estatio.dom.agreement.Agreement " + 
                    "WHERE agreementType == :agreementType" +
                    " && roles.contains(role)" +
                    " && role.type == :roleType" +
                    " && role.party == :party" +
                    " VARIABLES org.estatio.dom.agreement.AgreementRole role")
})
@Bookmarkable
@MemberGroups({"General", "Dates", "Related"})
public abstract class Agreement<S extends Lockable> extends EstatioTransactionalObject<Agreement<S>, S> implements WithReferenceComparable<Agreement<S>>, WithIntervalMutable<Agreement<S>>, WithNameGetter {

    public Agreement(S statusToLock, S statusToUnlock) {
        super("reference", statusToLock, statusToUnlock);
    }

    
    // //////////////////////////////////////

    @javax.jdo.annotations.Unique(name = "AGREEMENT_REFERENCE_UNIQUE_IDX")
    private String reference;

    @DescribedAs("Unique reference code for this agreement")
    @MemberOrder(sequence = "1")
    @Title
    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public String disableReference() {
        return getStatus().isLocked()? "Cannot modify when locked": null;
    }
    
    // //////////////////////////////////////

    private String name;

    @DescribedAs("Optional name for this agreement")
    @MemberOrder(sequence = "2")
    @Hidden(where=Where.ALL_TABLES)
    @Optional
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String disableName() {
        return getStatus().isLocked()? "Cannot modify when locked": null;
    }

    // //////////////////////////////////////

    @MemberOrder(sequence = "3")
    public abstract Party getPrimaryParty();

    @MemberOrder(sequence = "4")
    public abstract Party getSecondaryParty();

    // //////////////////////////////////////

    protected Party findParty(final String agreementRoleTypeTitle) {
        final AgreementRoleType art = agreementRoleTypes.findByTitle(agreementRoleTypeTitle);
        return findParty(art);
    }

    protected Party findParty(AgreementRoleType agreementRoleType) {
        final Predicate<AgreementRole> currentAgreementRoleOfType = currentAgreementRoleOfType(agreementRoleType);
        final Iterable<Party> parties = Iterables.transform(Iterables.filter(getRoles(), currentAgreementRoleOfType), partyOfAgreementRole());
        return ValueUtils.firstElseNull(parties);
    }

    private static Function<AgreementRole, Party> partyOfAgreementRole() {
        return new Function<AgreementRole, Party>() {
            public Party apply(AgreementRole agreementRole) {
                return agreementRole != null? agreementRole.getParty(): null;
            }
        };
    }

    private static Predicate<AgreementRole> currentAgreementRoleOfType(final AgreementRoleType art) {
        return new Predicate<AgreementRole>() {
            public boolean apply(AgreementRole candidate) {
                return candidate != null? candidate.getType() == art && candidate.isCurrent(): false;
            }
        };
    }


    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent
    private LocalDate startDate;

    @MemberOrder(name="Dates", sequence = "5")
    @Disabled
    @Optional
    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    @javax.jdo.annotations.Persistent
    private LocalDate endDate;

    @MemberOrder(name="Dates", sequence = "6")
    @Disabled
    @Optional
    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    // //////////////////////////////////////
    
    @Programmatic
    public LocalDateInterval getInterval() {
        return LocalDateInterval.including(getStartDate(), getEndDate());
    }
    
    // //////////////////////////////////////

    @MemberOrder(name="endDate", sequence="1")
    @ActionSemantics(Of.IDEMPOTENT)
    @Override
    public Agreement<S> changeDates(
            final @Named("Start Date") LocalDate startDate, 
            final @Named("End Date") LocalDate endDate) {
        setStartDate(startDate);
        setEndDate(endDate);
        return this;
    }

    @Override
    public String disableChangeDates(
            final LocalDate startDate, 
            final LocalDate endDate) {
        return getStatus().isLocked()? "Cannot modify when locked": null;
    }
    
    @Override
    public LocalDate default0ChangeDates() {
        return getStartDate();
    }
    @Override
    public LocalDate default1ChangeDates() {
        return getEndDate();
    }
    
    @Override
    public String validateChangeDates(
            final LocalDate startDate, 
            final LocalDate endDate) {
        return startDate.isBefore(endDate)?null:"Start date must be before end date";
    }

    // //////////////////////////////////////
    
    @Programmatic
    public LocalDateInterval getEffectiveInterval() {
        return LocalDateInterval.including(getStartDate(), getTerminationDate());
    }

    // //////////////////////////////////////
    
    @javax.jdo.annotations.Persistent
    private LocalDate terminationDate;
    
    @MemberOrder(name="Dates", sequence = "7")
    @Optional
    @Disabled
    public LocalDate getTerminationDate() {
        return terminationDate;
    }
    
    public void setTerminationDate(final LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }
    
    // //////////////////////////////////////

    @javax.jdo.annotations.Column(name="AGREEMENTTYPE_ID")
    private AgreementType agreementType;

    @Hidden(where=Where.ALL_TABLES)
    @Disabled
    @MemberOrder(sequence = "8")
    public AgreementType getAgreementType() {
        return agreementType;
    }

    public void setAgreementType(final AgreementType type) {
        this.agreementType = type;
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Column(name="PREVIOUS_ID")
    @javax.jdo.annotations.Persistent(mappedBy = "next")
    private Agreement<S> previous;

    @MemberOrder(name="Related", sequence = "9")
    @Named("Previous Agreement")
    @Hidden(where=Where.ALL_TABLES)
    @Disabled
    @Optional
    @Override
    public Agreement<S> getPrevious() {
        return previous;
    }

    public void setPrevious(final Agreement<S> previous) {
        this.previous = previous;
    }

    public void modifyPrevious(final Agreement<S> previous) {
        Agreement<S> currentPrevious = getPrevious();
        // check for no-op
        if (previous == null || previous.equals(currentPrevious)) {
            return;
        }
        // dissociate existing
        clearPrevious();
        // associate new
        previous.setNext(this);
        setPrevious(previous);
    }

    public void clearPrevious() {
        Agreement<S> currentPreviousAgreement = getPrevious();
        // check for no-op
        if (currentPreviousAgreement == null) {
            return;
        }
        // dissociate existing
        currentPreviousAgreement.setNext(null);
        setPrevious(null);
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Column(name="NEXT_ID")
    private Agreement<S> next;

    @MemberOrder(name="Related", sequence = "10")
    @Named("Next Agreement")
    @Hidden(where=Where.ALL_TABLES)
    @Disabled
    @Optional
    @Override
    public Agreement<S> getNext() {
        return next;
    }

    public void setNext(final Agreement<S> next) {
        this.next = next;
    }

    public void modifyNext(final Agreement<S> next) {
        Agreement<S> currentNext = getNext();
        // check for no-op
        if (next == null || next.equals(currentNext)) {
            return;
        }
        // delegate to parent(s) to (re-)associate
        if (currentNext != null) {
            currentNext.clearPrevious();
        }
        next.modifyPrevious(this);
    }

    public void clearNext() {
        Agreement<S> currentNext = getNext();
        // check for no-op
        if (currentNext == null) {
            return;
        }
        // delegate to parent to dissociate
        currentNext.clearPrevious();
    }

    // //////////////////////////////////////

    @javax.jdo.annotations.Persistent(mappedBy = "agreement")
    private SortedSet<AgreementRole> roles = new TreeSet<AgreementRole>();

    @MemberOrder(name = "Roles", sequence = "11")
    @Disabled
    @Render(Type.EAGERLY)
    public SortedSet<AgreementRole> getRoles() {
        return roles;
    }

    public void setRoles(final SortedSet<AgreementRole> actors) {
        this.roles = actors;
    }

    public void addToRoles(final AgreementRole agreementRole) {
        // check for no-op
        if (agreementRole == null || getRoles().contains(agreementRole)) {
            return;
        }
        // associate new
        getRoles().add(agreementRole);
        agreementRole.setAgreement(this);
    }

    public void removeFromRoles(final AgreementRole agreementRole) {
        // check for no-op
        if (agreementRole == null || !getRoles().contains(agreementRole)) {
            return;
        }
        // dissociate existing
        getRoles().remove(agreementRole);
        agreementRole.setAgreement(null);
    }

    /**
     * TODO: need logic ensure that there cannot be two {@link AgreementRole}s 
     * of the same type at the same point in time.
     */
    @MemberOrder(name = "Roles", sequence = "11")
    public AgreementRole addRole(@Named("party") Party party, @Named("agreementType") AgreementRoleType type, @Named("startDate") @Optional LocalDate startDate, @Named("endDate") @Optional LocalDate endDate) {
        AgreementRole agreementRole = findRole(party, type, startDate);
        if (agreementRole == null) {
            agreementRole = agreementRoles.newAgreementRole(this, party, type, startDate, endDate);
        }
        agreementRole.setEndDate(endDate);
        agreementRole.setStatus(Status.UNLOCKED);
        return agreementRole;
    }

    
    // //////////////////////////////////////

    @Programmatic
    AgreementRole findRole(Party party, AgreementRoleType type, LocalDate startDate) {
        return agreementRoles.findByAgreementAndPartyAndTypeAndStartDate(this, party, type, startDate);
    }

    @Programmatic
    public AgreementRole findRoleWithType(AgreementRoleType agreementRoleType, LocalDate date) {
        return agreementRoles.findByAgreementAndTypeAndContainsDate(this, agreementRoleType, date);
    }

    // //////////////////////////////////////

    protected Agreements agreements;
    public void injectAgreements(Agreements agreements) {
        this.agreements = agreements;
    }
    protected AgreementRoles agreementRoles;
    public void injectAgreementRoles(final AgreementRoles agreementRoles) {
        this.agreementRoles = agreementRoles;
    }

    protected AgreementRoleTypes agreementRoleTypes;
    public void injectAgreementRoleTypes(final AgreementRoleTypes agreementRoleTypes) {
        this.agreementRoleTypes = agreementRoleTypes;
    }

    protected AgreementTypes agreementTypes;
    public void injectAgreementTypes(AgreementTypes agreementTypes) {
        this.agreementTypes = agreementTypes;
    }

}
