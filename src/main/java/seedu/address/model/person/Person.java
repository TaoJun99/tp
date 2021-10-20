package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.*;

import seedu.address.model.assignment.*;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Email email;

    // Data fields
    private final Module module;
    private final Set<Tag> tags = new HashSet<>();
    private final UniqueAssignmentList assignments;


    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Email email, Module module, Set<Tag> tags) {
        requireAllNonNull(name, email, module, tags);
        this.name = name;
        this.email = email;
        this.module = module;
        this.tags.addAll(tags);
        assignments = new UniqueAssignmentList();
    }

    /**
     * Constructor for creating a person with assignments
     */
    public Person(Name name, Email email, Module module, List<Assignment> assignmentList, Set<Tag> tags) {
        requireAllNonNull(name, email, module, tags);
        this.name = name;
        this.email = email;
        this.module = module;
        this.tags.addAll(tags);
        assignments = new UniqueAssignmentList();
        for (Assignment assignment:assignmentList) {
            assignments.add(assignment);
        }
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Module getModule() {
        return module;
    }

    public UniqueAssignmentList getAssignments() {
        assignments.sort();
        return assignments;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && isSameName(otherPerson.getName());
    }

    public boolean isSameName(Name name) {
        return this.getName().equals(name);
    }

    public boolean hasModule(Module module) {
        return this.getModule().equals(module);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Person)) {
            return false;
        }

        Person otherPerson = (Person) other;
        return otherPerson.getName().equals(getName())
                && otherPerson.getEmail().equals(getEmail())
                && otherPerson.getModule().equals(getModule())
                && otherPerson.getAssignments().equals(getAssignments())
                && otherPerson.getTags().equals(getTags());
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(assignments, name, email, module, tags);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getName())
                .append("; Email: ")
                .append(getEmail())
                .append("; Module: ")
                .append(getModule());

        UniqueAssignmentList assignments = getAssignments();
        if (!assignments.isEmpty()) {
            builder.append("; Assignments: ");
            assignments.asUnmodifiableObservableList().forEach(builder::append);
        }

        Set<Tag> tags = getTags();
        if (!tags.isEmpty()) {
            builder.append("; Tags: ");
            tags.forEach(builder::append);
        }
        return builder.toString();
    }
}
