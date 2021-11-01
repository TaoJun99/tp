package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final ObservableList<Assignment> assignmentsList;
    private final VersionedAddressBook versionedAddressBook;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.versionedAddressBook = new VersionedAddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.versionedAddressBook.getPersonList());
        assignmentsList = new FilteredList<>(this.versionedAddressBook.getAssignmentsList());
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.versionedAddressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return versionedAddressBook;
    }

    //=========== Person ================================================================================

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return versionedAddressBook.hasPerson(person);
    }

    @Override
    public boolean hasExistingEmail(Person person) {
        requireNonNull(person);
        return versionedAddressBook.hasEmail(person);
    }

    @Override
    public void deletePerson(Person target) {
        versionedAddressBook.removePerson(target);

        /*show an empty assignment list in AddressBook if the person deleted has his/her assignments
            stored in AddressBook's Assignment List*/
        if (versionedAddressBook.isActivePerson(target)) {
            updateFilteredAssignmentList(target);
        }
    }

    @Override
    public void addPerson(Person person) {
        versionedAddressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);
        versionedAddressBook.setPerson(target, editedPerson);
    }

    //=========== Assignment ================================================================================

    @Override
    public boolean hasAssignment(Person person, Assignment toAdd) {
        return versionedAddressBook.hasAssignment(person, toAdd);
    }

    @Override
    public void addAssignment(Person person, Assignment toAdd) {
        versionedAddressBook.addAssignment(person, toAdd);
        updateFilteredAssignmentList(person);
    }

    @Override
    public void addAllAssignment(List<Person> personList, Assignment toAdd) {
        for (Person person: personList) {
            if (!hasAssignment(person, toAdd)) {
                versionedAddressBook.addAssignment(person, toAdd);
            }
        }
        if (hasActivePerson()) {
            updateFilteredAssignmentList(getActivePerson());
        }
    }

    @Override
    public void deleteAssignment(Person person, Assignment toDelete) {
        versionedAddressBook.removeAssignment(person, toDelete);
        updateFilteredAssignmentList(person);
    }

    @Override
    public void markAssignment(Person person, Assignment toMark) {
        versionedAddressBook.markAssignment(person, toMark);
        updateFilteredAssignmentList(person);
    }

    @Override
    public void cleanAssignments() {
        versionedAddressBook.cleanAssignments();
        if (hasActivePerson()) {
            updateFilteredAssignmentList(getActivePerson());
        }
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        versionedAddressBook.setFilteredPersonListPredicate(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;

        return versionedAddressBook.equals(other.versionedAddressBook)
                && userPrefs.equals(other.userPrefs)
                && filteredPersons.equals(other.filteredPersons);
    }

    //=========== Filtered Assignment List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Assignment} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Assignment> getFilteredAssignmentList() {
        return assignmentsList;
    }

    @Override
    public List<Assignment> getFilteredAssignmentList(Person person) {
        requireNonNull(person);
        return this.versionedAddressBook.getPersonAssignmentList(person);
    }

    @Override
    public void updateFilteredAssignmentList(Person person) {
        this.versionedAddressBook.changeActivePerson(person);
        this.versionedAddressBook.updateAssignmentList(person);
    }

    //=========== Active Person =========================================================================
    public Person getActivePerson() {
        return versionedAddressBook.getActivePerson();
    }

    public boolean hasActivePerson() {
        return versionedAddressBook.hasActivePerson();
    }

    @Override
    public void commitAddressBook(ReadOnlyAddressBook addressBook) {
        versionedAddressBook.commitAddressBook(addressBook);
    }

    @Override
    public void undoAddressBook() throws CommandException {
        versionedAddressBook.undo();
        updateFilteredPersonList(this.versionedAddressBook.getFilteredPersonListPredicate());
    }

    @Override
    public void redoAddressBook() throws CommandException {
        versionedAddressBook.redo();
        updateFilteredPersonList(this.versionedAddressBook.getFilteredPersonListPredicate());
    }

    @Override
    public VersionedAddressBook getVersionedAddressBook() {
        return new VersionedAddressBook(getAddressBook());
    }
}
