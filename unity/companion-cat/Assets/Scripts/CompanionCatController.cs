using System;
using System.Collections.Generic;
using UnityEngine;

namespace Maomaomao.CompanionCat
{
    public sealed class CompanionCatController : MonoBehaviour
    {
        private static readonly HashSet<string> SupportedActions = new HashSet<string>(StringComparer.OrdinalIgnoreCase)
        {
            "Idle",
            "Observe",
            "Pet",
            "Drink",
            "Eat",
            "Happy"
        };

        [SerializeField] private Animator animator;
        [SerializeField] private string idleStateName = "Idle";
        [SerializeField] private float crossFadeDuration = 0.15f;

        public string CurrentAction { get; private set; } = "Idle";

        private void Reset()
        {
            animator = GetComponentInChildren<Animator>();
        }

        private void Awake()
        {
            if (animator == null)
            {
                animator = GetComponentInChildren<Animator>();
            }

            PlayAction(idleStateName);
        }

        public void PlayAction(string actionName)
        {
            string normalizedAction = NormalizeActionName(actionName);
            CurrentAction = normalizedAction;

            if (animator == null)
            {
                Debug.LogWarning($"CompanionCatController cannot play '{normalizedAction}' because no Animator is assigned.", this);
                return;
            }

            animator.CrossFade(normalizedAction, crossFadeDuration, 0);
        }

        public void PlayIdle()
        {
            PlayAction("Idle");
        }

        public void PlayObserve()
        {
            PlayAction("Observe");
        }

        public void PlayPet()
        {
            PlayAction("Pet");
        }

        public void PlayDrink()
        {
            PlayAction("Drink");
        }

        public void PlayEat()
        {
            PlayAction("Eat");
        }

        private static string NormalizeActionName(string actionName)
        {
            if (string.IsNullOrWhiteSpace(actionName))
            {
                return "Idle";
            }

            string trimmedAction = actionName.Trim();
            return SupportedActions.Contains(trimmedAction) ? trimmedAction : "Idle";
        }
    }
}
