from rest_framework.serializers import ModelSerializer

from .models import Employee


class EmployeeSerializer(ModelSerializer):
    class Meta:
        model = Employee
        fields = ["id", "user", "employee_type"]

