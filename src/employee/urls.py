from django.urls import include, path
from rest_framework.routers import SimpleRouter
from employee.views import EmployeeViewSet

# Create a router and register our viewsets with it.
router = SimpleRouter(trailing_slash=False)
router.register(r"employees", EmployeeViewSet)

# The API URLs are now determined automatically by the router.
urlpatterns = [
    path("", include(router.urls)),
]
