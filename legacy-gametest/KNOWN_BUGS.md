# QuickShulker 3.0.4 known-behavior ledger

This file distinguishes compatibility contracts from historical defects. A
defect is never silently promoted to intended API behavior.

| ID | 3.0.4 behavior | Current expectation |
| --- | --- | --- |
| QS-LB-001 | The deprecated `requiresSingularStack` registration argument is ignored. | Preserved for binary/behavioral compatibility; callers should use `QuickShulkerData.ignoreSingleStackCheck`. |
| QS-LB-002 | Box-to-box transfer casts both registered containers to `SimpleContainer`. | Any valid Minecraft `Container` works. |
| QS-LB-003 | Box-to-box transfer bypasses the destination registration's insertion policy. | The destination policy is enforced for every moved stack. |
| QS-LB-004 | A successful box-to-box transfer with a null mixin callback skips `stopOpen()`. | Persistence is independent of whether a callback object is supplied. |
| QS-LB-005 | `shouldAttemptUnBundle` resolves the inventory even when the feature is disabled or the click is ineligible. | Currently retained; it is observable through custom registry callbacks. |
| QS-LB-006 | Calling `Util.openItem(player, -1)` logs and then indexes slot `-1`. | Currently retained as a documented invalid-input failure. |
| QS-LB-007 | Unbundling stops at the last item accepted by `Slot.mayPlace`, even if `safeInsert` moves zero because the slot is full or contains another item. It still reports success. | Zero-move candidates are skipped; a compatible earlier item may be used, otherwise the operation reports no transfer. |

The GameTest method carrying an ID is the executable evidence for that row.
