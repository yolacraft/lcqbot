# Bug Analysis: "When running start tracking for the 2nd time, it starts seed 1 again, even tho it has been confirmed"

## Summary
When `/start_tracking` is executed a second time, the system creates a new seed with the same number instead of skipping to the next seed. The confirmed seed is preserved, but a new PENDING seed with the same `seedNumber` is created.

---

## Root Causes

### 🔴 PRIMARY BUG: Event.currentSeed Is Never Persisted

**File:** [StartTrackingCommandHandler.java](StartTrackingCommandHandler.java#L24-L28)

```java
// ❌ BUG: currentSeed is incremented in memory only
int currentSeed = event.getCurrentSeed() + 1;
event.setCurrentSeed(currentSeed);

// ❌ MISSING: storage.saveEvent(event); 
// The incremented value is NEVER written to storage!

trackingService.startTracking(event, host, currentSeed);
```

**What happens:**

1. **First `/start_tracking` execution:**
   - Event is loaded from storage (has `currentSeed = 0`)
   - Code increments it to `1` (in memory only)
   - Passes `seedNumber = 1` to `trackingService.startTracking()`
   - **Event is never saved** → Storage still has `currentSeed = 0`

2. **Second `/start_tracking` execution:**
   - Event is reloaded from storage fresh (still `currentSeed = 0`)
   - Code increments it to `1` again (in memory)
   - Passes `seedNumber = 1` again
   - Creates another seed with `seedNumber = 1` and `status = PENDING`
   - Now TWO seeds exist with `seedNumber = 1`: one CONFIRMED, one PENDING

**The Fix:**
```java
// After incrementing currentSeed, SAVE the event
int currentSeed = event.getCurrentSeed() + 1;
event.setCurrentSeed(currentSeed);
storage.saveEvent(event);  // ← ADD THIS LINE

trackingService.startTracking(event, host, currentSeed);
```

---

### 🟡 SECONDARY BUG: MatchTrackingService Creates Duplicate Seeds

**File:** [MatchTrackingService.java](MatchTrackingService.java#L72-L80)

```java
// ❌ BUG: No check for existing seeds with same seedNumber
Seed seed = new Seed();
seed.setId(UUID.randomUUID().toString());
seed.setEventId(event.getId());
seed.setSeedNumber(seedNumber);
seed.setStatus(SeedStatus.PENDING);  // Always PENDING, ignoring confirmed state
seed.setPlacements(placements);
seed.setStartedAt(System.currentTimeMillis());

storage.saveSeed(seed);  // ← Creates a new seed every time, even if one with this number exists
```

**The Problem:**

1. The method doesn't check if a seed with `seedNumber` already exists
2. It always creates a **new** Seed with a **new UUID**
3. If a confirmed seed exists with the same `seedNumber`, both will coexist in storage

**The Fix:**
```java
// Check if a seed with this number already exists
Optional<Seed> existingSeed = storage.loadSeeds(event.getId()).stream()
    .filter(s -> s.getSeedNumber() == seedNumber)
    .findFirst();

Seed seed;
if (existingSeed.isPresent()) {
    // Reuse existing seed, just update placements if it's still PENDING
    seed = existingSeed.get();
    if (seed.getStatus() != SeedStatus.PENDING) {
        // Skip if already confirmed
        return;
    }
} else {
    // Create new seed only if it doesn't exist
    seed = new Seed();
    seed.setId(UUID.randomUUID().toString());
    seed.setEventId(event.getId());
    seed.setSeedNumber(seedNumber);
}

seed.setStatus(SeedStatus.PENDING);
seed.setPlacements(placements);
seed.setStartedAt(System.currentTimeMillis());
storage.saveSeed(seed);
```

---

## Data Flow Showing the Bug

### Scenario: Running `/start_tracking` twice for same event

```
=== FIRST /start_tracking ===
1. Load Event from storage: { currentSeed: 0, ... }
2. Increment in memory: currentSeed = 1
3. ❌ DON'T save the event
4. Create Seed#1 with status PENDING
5. User confirms: Seed#1 status → CONFIRMED
6. Storage state:
   - Event { currentSeed: 0 }  ← Still 0!
   - Seed#1 { status: CONFIRMED }

=== SECOND /start_tracking ===
7. Reload Event from storage: { currentSeed: 0 }  ← Fresh reload, gets original value
8. Increment in memory: currentSeed = 1  ← Same as before
9. ❌ DON'T save the event again
10. Create NEW Seed#1 with status PENDING  ← Duplicate!
11. Storage state:
    - Event { currentSeed: 0 }  ← Still 0!
    - Seed#1 { status: CONFIRMED }  ← From step 5
    - Seed#1 { status: PENDING }    ← From step 10 (different UUID, same seedNumber!)
```

---

## Affected Code Locations

| Issue | File | Lines | Problem |
|-------|------|-------|---------|
| **Primary** | [StartTrackingCommandHandler.java](StartTrackingCommandHandler.java#L24-L28) | 24-28 | `event.setCurrentSeed()` but no `storage.saveEvent()` |
| **Secondary** | [MatchTrackingService.java](MatchTrackingService.java#L72-L80) | 72-80 | Creates new seed without checking if one exists |
| Related | [Event.java](Event.java#L18) | 18 | `currentSeed` field initialized to 0 |

---

## How Seed State Management Should Work

**Current (Broken) Behavior:**
```
Event.currentSeed (not persisted) → Used to create Seed.seedNumber
Seed persists with SeedStatus
But currentSeed never updates in storage → creates duplicates
```

**Expected (Fixed) Behavior:**
```
Event.currentSeed (MUST be persisted) → Incremented and saved after each start_tracking
Seed persists with SeedStatus
Second start_tracking uses NEW currentSeed value → no duplicates
```

---

## Summary of Required Fixes

1. **In StartTrackingCommandHandler.java (Line 28):**
   - Add `storage.saveEvent(event);` after incrementing `currentSeed`

2. **In MatchTrackingService.java (Line 72-80):**
   - Before creating new seed, check if seed with same `seedNumber` already exists
   - If it exists and status is CONFIRMED, abort or skip
   - If it exists and status is PENDING, update it instead of creating duplicate

3. **Optional:** Add validation in ConfirmSeedCommandHandler to ensure only one PENDING seed exists per number
