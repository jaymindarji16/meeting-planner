# AI Usage Log

A short record of the prompts that materially shaped decisions during this
exercise.

---

1. I'm modeling meeting participants as a join table with (meeting_id,
   user_id). Where should the RSVP status live — on the join row, or
   denormalized onto Meeting?

2. How do I hash passwords in a Spring Boot prototype without pulling in the
   full Spring Security filter chain?

3. I have a MeetingService where visibility rules (who can view/edit/delete)
   are inline. Should I extract them into a separate class, or is that
   over-engineering for a prototype?

4. Reviewing this JPQL: `select distinct m from Meeting m left join
   m.participants p where m.organizer.id = :userId or p.user.id = :userId`.
   It compiles but returns the wrong rows on Hibernate 7. Is the query wrong,
   or is this a version-specific issue?

5. Hibernate is throwing LazyInitializationException on a @OneToMany
   collection after the transaction closes. Is @Transactional on the read
   method the right fix, or should I be using a fetch join instead?

6. Reviewing my Angular route setup: is authGuard alone enough for protected
   routes, or do I also need a guestGuard on /login and /signup?

7. What's the right way to persist a JPA collection whose parent entity was
   created in the same transaction — cascade ALL + orphanRemoval, or save
   children explicitly?

8. I'm validating avatar uploads by file extension only. What's the minimum
   set of checks I should add to make this safe enough for a prototype
   without pulling in a heavyweight library?
