import { Button, Form } from "react-bootstrap";
import { useForm } from "react-hook-form";

export default function CreateCommonsForm({initialCommon, submitAction, buttonLabel = "Create"}) {
  const { onSubmit } = submitAction;
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm(
    {defaultValues: {...initialCommon, startDate: initialCommon?.startDate.substr(0,10)} || {}}
    );

  return (
    <Form onSubmit={handleSubmit(onSubmit)}>

      {initialCommon && (
          <Form.Group className="mb-3" >
              <Form.Label htmlFor="id">Id</Form.Label>
              <Form.Control
                  data-testid="CreateCommonsForm-id"
                  id="id"
                  type="text"
                  {...register("id")}
                  value={initialCommon.id}
                  disabled
              />
          </Form.Group>
      )}

      <Form.Group className="mb-3">
        <Form.Label htmlFor="name">Commons Name</Form.Label>
        <Form.Control
          data-test-id="CreateCommonsForm-name"
          id="name"
          type="text"
          isInvalid={!!errors.name}
          {...register("name", { required: "Commons name is required" })}
        />
        <Form.Control.Feedback type="invalid">
          {errors.name?.message}
        </Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label htmlFor="startingBalance">Starting Balance</Form.Label>
        <Form.Control
          data-test-id="CreateCommonsForm-startbal"
          id="startingBalance"
          type="number"
          step="0.01"
          isInvalid={!!errors.startingBalance}
          {...register("startingBalance", {
            valueAsNumber: true,
            required: "Starting Balance is required",
            min: { value: 0.01, message: "Starting Balance must be positive" },
          })}
        />
        <Form.Control.Feedback type="invalid">
          {errors.startingBalance?.message}
        </Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label htmlFor="cowPrice">Cow Price</Form.Label>
        <Form.Control
          data-test-id="CreateCommonsForm-cowprice"
          id="cowPrice"
          type="number"
          step="0.01"
          isInvalid={!!errors.cowPrice}
          {...register("cowPrice", {
            valueAsNumber: true,
            required: "Cow price is required",
            min: { value: 0.01, message: "Cow price must be positive" },
          })}
        />
        <Form.Control.Feedback type="invalid">
          {errors.cowPrice?.message}
        </Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label htmlFor="milkPrice">Milk Price</Form.Label>
        <Form.Control
          data-test-id="CreateCommonsForm-milkprice"
          id="milkPrice"
          type="number"
          step="0.01"
          isInvalid={!!errors.milkPrice}
          {...register("milkPrice", {
            valueAsNumber: true,
            required: "Milk price is required",
            min: { value: 0.01, message: "Milk price must be positive" },
          })}
        />
        <Form.Control.Feedback type="invalid">
          {errors.milkPrice?.message}
        </Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label htmlFor="startDate">Start Date</Form.Label>
        <Form.Control 
          data-test-id="CreateCommonsForm-startdate"
          id="startDate"
          type="date"
          // defaultValue={initialCommon.startDate.substr(0,10)}
          defaultValue={"2022-01-01"}
          isInvalid={!!errors.startDate}
          {...register("startDate", {
            valueAsDate: true,
            validate: {
              isPresent: (v) => !isNaN(v) || "Start date is required",
            },            
          })}
        />
        <Form.Control.Feedback type="invalid">
          {errors.startDate?.message}
        </Form.Control.Feedback>
      </Form.Group>
      <Button type="submit" data-testid="CreateCommonsForm-Create-Button">
        {buttonLabel}
      </Button>

    </Form>
  );
}
