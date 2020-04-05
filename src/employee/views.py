from django.shortcuts import render

from rest_framework.viewsets import ModelViewSet

from commons.schemas import CamelCaseSchema

from .models import Employee
from .serializers import EmployeeSerializer

# Create your views here.
class EmployeeViewSet(ModelViewSet):
    schema = CamelCaseSchema()
    queryset = Employee.objects.all()
    serializer_class = EmployeeSerializer
