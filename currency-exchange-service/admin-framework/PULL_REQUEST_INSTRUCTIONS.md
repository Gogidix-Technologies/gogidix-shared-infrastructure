# Pull Request Instructions

A new branch `github-config` has been created with the implementation of the Admin Framework components. To merge these changes into the `main` branch, follow these steps:

1. Go to the GitHub repository: https://github.com/Micro-Services-Social-Ecommerce-App/admin-framework

2. Click on "Pull requests" tab

3. Click on "New pull request"

4. Set the base branch to `main` and the compare branch to `github-config`

5. Click "Create pull request"

6. Add a title like "Add Admin Framework Implementation"

7. Add a description similar to:
   ```
   This PR adds the implementation of the Admin Framework components to address code duplication in admin dashboards across domains.

   ### Added Components
   - Dashboard components (controllers, services, models)
   - Policy components (controllers, services, models)
   - Utility package with export format
   - Directory structure for data access and integration

   ### Framework Structure
   The Admin Framework provides abstract base classes that domain-specific implementations will extend:
   - Abstract controllers for common endpoints
   - Abstract services for business logic
   - Base models for data representation

   This implementation helps address the code duplication issues identified in the audit.
   ```

8. Review the changes

9. Merge the pull request

After merging, the GitHub repository will be in sync with the local implementation.
